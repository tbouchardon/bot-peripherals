package fr.ksuto.prh.tools;

import fr.ksuto.prh.entities.Parameter;
import fr.ksuto.prh.helpers.ParameterLearning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mémoire des recherches d'objets à l'écran, dans une base SQLite (un simple fichier, sans serveur) : pour chaque
 * recherche, identifiée par une clé (images cherchées et résolution de l'écran),
 * <ul>
 *   <li>les statistiques de chaque combinaison de tolérances (apprentissage, {@link ParameterLearning}) ;</li>
 *   <li>les dernières positions où les objets ont été trouvés, et la zone de recherche réduite qui en découle.</li>
 * </ul>
 * Une seule connexion, partagée et synchronisée ; les écritures d'une recherche sont groupées dans une transaction. Une
 * erreur de base est journalisée sans interrompre le bot : il cherche alors sans mémoire.
 */
public final class SearchMemory implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(SearchMemory.class);

    private static final String SCHEMA = """
            CREATE TABLE IF NOT EXISTS search_area (
                search_key TEXT PRIMARY KEY,
                x INTEGER NOT NULL, y INTEGER NOT NULL, width INTEGER NOT NULL, height INTEGER NOT NULL);
            CREATE TABLE IF NOT EXISTS search_parameter (
                search_key TEXT NOT NULL,
                precision INTEGER NOT NULL,
                error_rate REAL NOT NULL,
                trials INTEGER NOT NULL,
                successes INTEGER NOT NULL,
                PRIMARY KEY (search_key, precision, error_rate));
            CREATE TABLE IF NOT EXISTS search_position (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                search_key TEXT NOT NULL,
                x INTEGER NOT NULL, y INTEGER NOT NULL,
                seen_at INTEGER NOT NULL);
            CREATE INDEX IF NOT EXISTS search_position_key ON search_position (search_key, id);
            """;

    private final Connection connection;

    private SearchMemory(String url) throws SQLException {

        this.connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            for (String sql : SCHEMA.split(";")) {
                if (!sql.isBlank()) {statement.execute(sql);}
            }
        }
    }

    /**
     * @param file fichier de la base, créé s'il n'existe pas (dossiers compris)
     */
    public static SearchMemory open(Path file) throws SQLException {

        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {Files.createDirectories(parent);}
        }
        catch (IOException e) {
            throw new SQLException("Dossier de la base inaccessible : " + file, e);
        }
        return new SearchMemory("jdbc:sqlite:" + file.toAbsolutePath());
    }

    /**
     * Mémoire sans fichier, perdue à la fermeture : mode hors ligne, tests.
     */
    public static SearchMemory inMemory() {

        try {
            return new SearchMemory("jdbc:sqlite::memory:");
        }
        catch (SQLException e) {
            throw new IllegalStateException("SQLite indisponible", e);
        }
    }

    // --- Tolérances ---

    public synchronized Map<Parameter, ParameterLearning.Stat> parameterStats(String key) {

        Map<Parameter, ParameterLearning.Stat> stats = new HashMap<>();
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT precision, error_rate, trials, successes FROM search_parameter WHERE search_key = ?")) {
            query.setString(1, key);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    stats.put(new Parameter(rows.getInt(1), rows.getDouble(2)), new ParameterLearning.Stat(rows.getInt(3), rows.getInt(4)));
                }
            }
        }
        catch (SQLException e) {
            logger.warn("Mémoire des recherches illisible ({}) : {}", key, e.getMessage());
        }
        return stats;
    }

    /**
     * Ajoute un essai à chaque combinaison : réussi si elle a trouvé le nombre d'objets attendu.
     */
    public synchronized void recordTrials(String key, Map<Parameter, Boolean> outcomes) {

        inTransaction(() -> {
            try (PreparedStatement upsert = connection.prepareStatement("""
                    INSERT INTO search_parameter (search_key, precision, error_rate, trials, successes) VALUES (?, ?, ?, 1, ?)
                    ON CONFLICT (search_key, precision, error_rate)
                    DO UPDATE SET trials = trials + 1, successes = successes + excluded.successes""")) {
                for (Map.Entry<Parameter, Boolean> outcome : outcomes.entrySet()) {
                    upsert.setString(1, key);
                    upsert.setInt(2, outcome.getKey().precision());
                    upsert.setDouble(3, outcome.getKey().errorRate());
                    upsert.setInt(4, outcome.getValue() ? 1 : 0);
                    upsert.addBatch();
                }
                upsert.executeBatch();
            }
        });
    }

    public synchronized void clearParameters(String key) {

        update("DELETE FROM search_parameter WHERE search_key = ?", key);
    }

    // --- Positions et zone de recherche ---

    /**
     * Ajoute des positions trouvées et ne garde que les {@code keep} plus récentes.
     */
    public synchronized void addPositions(String key, List<Point> positions, int keep) {

        if (positions.isEmpty()) {return;}
        inTransaction(() -> {
            long now = System.currentTimeMillis();
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO search_position (search_key, x, y, seen_at) VALUES (?, ?, ?, ?)")) {
                for (Point position : positions) {
                    insert.setString(1, key);
                    insert.setInt(2, position.x);
                    insert.setInt(3, position.y);
                    insert.setLong(4, now);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            try (PreparedStatement prune = connection.prepareStatement("""
                    DELETE FROM search_position WHERE search_key = ? AND id NOT IN
                        (SELECT id FROM search_position WHERE search_key = ? ORDER BY id DESC LIMIT ?)""")) {
                prune.setString(1, key);
                prune.setString(2, key);
                prune.setInt(3, keep);
                prune.executeUpdate();
            }
        });
    }

    /**
     * @return les positions retenues, de la plus ancienne à la plus récente
     */
    public synchronized List<Point> positions(String key) {

        List<Point> positions = new ArrayList<>();
        try (PreparedStatement query = connection.prepareStatement("SELECT x, y FROM search_position WHERE search_key = ? ORDER BY id")) {
            query.setString(1, key);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {positions.add(new Point(rows.getInt(1), rows.getInt(2)));}
            }
        }
        catch (SQLException e) {
            logger.warn("Mémoire des recherches illisible ({}) : {}", key, e.getMessage());
        }
        return positions;
    }

    public synchronized Optional<Rectangle> area(String key) {

        try (PreparedStatement query = connection.prepareStatement("SELECT x, y, width, height FROM search_area WHERE search_key = ?")) {
            query.setString(1, key);
            try (ResultSet rows = query.executeQuery()) {
                if (rows.next()) {return Optional.of(new Rectangle(rows.getInt(1), rows.getInt(2), rows.getInt(3), rows.getInt(4)));}
            }
        }
        catch (SQLException e) {
            logger.warn("Mémoire des recherches illisible ({}) : {}", key, e.getMessage());
        }
        return Optional.empty();
    }

    public synchronized void setArea(String key, Rectangle area) {

        try (PreparedStatement upsert = connection.prepareStatement("""
                INSERT INTO search_area (search_key, x, y, width, height) VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (search_key) DO UPDATE SET x = excluded.x, y = excluded.y, width = excluded.width, height = excluded.height""")) {
            upsert.setString(1, key);
            upsert.setInt(2, area.x);
            upsert.setInt(3, area.y);
            upsert.setInt(4, area.width);
            upsert.setInt(5, area.height);
            upsert.executeUpdate();
        }
        catch (SQLException e) {
            logger.warn("Zone de recherche non enregistrée ({}) : {}", key, e.getMessage());
        }
    }

    /**
     * Oublie la zone réduite et les positions qui l'ont produite : la recherche reprend sur toute sa zone.
     */
    public synchronized void clearArea(String key) {

        update("DELETE FROM search_area WHERE search_key = ?", key);
        update("DELETE FROM search_position WHERE search_key = ?", key);
    }

    /**
     * Oublie tout ce qui concerne cette recherche.
     */
    public synchronized void clear(String key) {

        clearArea(key);
        clearParameters(key);
    }

    @Override
    public synchronized void close() {

        try {
            connection.close();
        }
        catch (SQLException e) {
            logger.debug("Fermeture de la mémoire des recherches : {}", e.getMessage());
        }
    }

    private interface SqlWork {

        void run() throws SQLException;
    }

    private void inTransaction(SqlWork work) {

        try {
            connection.setAutoCommit(false);
            try {
                work.run();
                connection.commit();
            }
            catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            finally {
                connection.setAutoCommit(true);
            }
        }
        catch (SQLException e) {
            logger.warn("Mémoire des recherches non mise à jour : {}", e.getMessage());
        }
    }

    private void update(String sql, String key) {

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            logger.warn("Mémoire des recherches non mise à jour ({}) : {}", key, e.getMessage());
        }
    }
}
