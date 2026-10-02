package fr.ksuto.prh.demo;

import fr.ksuto.prh.entities.LocatedObject;
import fr.ksuto.prh.entities.Position;
import fr.ksuto.prh.peripherals.Peripheral;
import fr.ksuto.prh.tools.ShowObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Démo manuelle (à lancer sous Windows) : affiche des objets localisés qui bougent légèrement, avec les marqueurs
 * « déplacé » et « exploré » de ShowObjects.
 */
public class ShowObjectsDemo {

    public static void main(String[] args) {

        ShowObjects<LocatedObject> showObjects = new ShowObjects<>();

        showObjects.countDown(3);

        List<Position> positions = new ArrayList<>();
        positions.add(new Position(100, 500));
        Position position = new Position(200, 400);
        position.setHasMoved(true);
        positions.add(position);
        positions.add(new Position(300, 300));
        position = new Position(400, 200);
        position.setExplored(true);
        positions.add(position);
        position = new Position(500, 100);
        position.setExplored(true);
        position.setHasMoved(true);
        positions.add(position);

        LocatedObject locatedObject = new LocatedObject() {

            @Override
            public String getHash() {

                return "demo";
            }
        };
        locatedObject.setWidth(10);
        locatedObject.setHeight(15);
        locatedObject.setPositions(positions);

        List<LocatedObject> locatedObjects = List.of(locatedObject);

        while (!showObjects.shouldClose()) {
            for (Position pos : locatedObject.getPositions()) {
                pos.setY(pos.getY() + (int) (Math.random() * 3 - 1.5));
                pos.setX(pos.getX() + (int) (Math.random() * 3 - 1.5));
            }
            showObjects.setLocatedObjects(locatedObjects);
            Peripheral.delay(25);
        }
    }
}
