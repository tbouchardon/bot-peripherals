package net.ddns.ksuto.prh.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

public class Arithmetic {
    
    public static double getOrdonnee(Point a, double m) {
        
        return getOrdonnee(new PrecisePoint(a), m);
    }
    
    public static double getPente(Point a, Point b) {
        
        return getPente(new PrecisePoint(a), new PrecisePoint(b));
    }
    
    public static PrecisePoint getPointAtDistanceOnLine(double x1, double y1, double m, double d, boolean top) {
        
        double p     = getOrdonnee(new PrecisePoint(x1, y1), m);
        double theta = getLineAngleWithAbscisse(new PrecisePoint(x1, y1), m, p);
        
        double x2, y2;
        
        if (top) {
            if (m > 0) {
                x2 = x1 - d * Math.cos(theta);
                y2 = y1 - d * Math.sin(theta);
            }
            else {
                x2 = x1 + d * Math.cos(theta);
                y2 = y1 - d * Math.sin(theta);
            }
        }
        else {
            if (m > 0) {
                x2 = x1 + d * Math.cos(theta);
                y2 = y1 + d * Math.sin(theta);
            }
            else {
                x2 = x1 - d * Math.cos(theta);
                y2 = y1 + d * Math.sin(theta);
            }
        }
        
        return new PrecisePoint(x2, y2);
    }
    
    /**
     * @param x1 x de A
     * @param y1 y de A
     * @param x2 x de B
     * @param y2 y de B
     *           <p>
     *           d=√((x_2-x_1)²+(y_2-y_1)²);
     *
     *           <pre>   |                                                             </pre>
     *           <pre>   |                                                             </pre>
     *           <pre>   |                                                             </pre>
     *           <pre>   |                   X  ==> A                                  </pre>
     *           <pre>   |                     \                                       </pre>
     *           <pre>   |                       \                                     </pre>
     *           <pre>   |                         \    dAB                            </pre>
     *           <pre>   |                           \                                 </pre>
     *           <pre>   |                             \                               </pre>
     *           <pre>   |                               X ==> B                       </pre>
     *           <pre>   |                                                             </pre>
     *           <pre>   |                                                             </pre>
     *           <pre> ----------------------------------------------------------------</pre>
     *           <pre>   |                                                             </pre>
     *           <pre>   |                                                             </pre>
     *           <pre>   |                                                             </pre>
     *
     * @return distance AB
     */
    public static double getDistance(double x1, double y1, double x2, double y2) {
        
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    public static double getCircleHeight(double length, double currentPosition, double flatenningScale) {
        
        double x = (2 / length * currentPosition) - 1;
        
        double yPositiveValueOnCircle = getYPositiveValueOnCircle(x);
        
        return yPositiveValueOnCircle * length * flatenningScale / 2;
    }
    
    /**
     * @param a Le point A
     * @param m la pente de la droite
     * @param p l'ordonnée dela droite
     *
     *
     *          <pre>   |           \                                                                        </pre>
     *          <pre>   |             \                                                                      </pre>
     *          <pre>   |               \                                                                    </pre>
     *          <pre>   |                 \                                                                  </pre>
     *          <pre>   |                   X  ==> A                                                         </pre>
     *          <pre>   |                   | \                                                              </pre>
     *          <pre>   |                   |   \                                                            </pre>
     *          <pre>   |                   |     \                                                          </pre>
     *          <pre>   |                   |       \                                                        </pre>
     *          <pre>   |                   |         \                                                      </pre>
     *          <pre>   |                   |           \                                                    </pre>
     *          <pre>   |                   |             \                                                  </pre>
     *          <pre>   |                   |               \                                                </pre>
     *          <pre>   |                   |                 \                                              </pre>
     *          <pre>   |                   |                 ( \ ==> θ                                      </pre>
     *          <pre> ----------------------X---------------------X------------------------------------------</pre>
     *          <pre>   |                   ╚==> A'          B <==╝ \                                        </pre>
     *          <pre>   |                                             \                                      </pre>
     *          <pre>   |                                               \                                    </pre>
     *
     * @return l'angle thêta entre la droite et l'abscisse
     */
    private static double getLineAngleWithAbscisse(PrecisePoint a, double m, double p) {
        
        double       xB     = -p / m;
        PrecisePoint b      = new PrecisePoint(xB, 0);
        PrecisePoint aPrime = new PrecisePoint(a.getX(), 0);
        
        double dAB      = getDistance(a.getX(), a.getY(), b.getX(), b.getY());
        double dAPrimeB = getDistance(aPrime.getX(), aPrime.getY(), b.getX(), b.getY());
        
        return Math.acos(dAPrimeB / dAB);
    }
    
    private static double getOrdonnee(PrecisePoint a, double m) {
        
        return -((m * a.getX()) - a.getY());
    }
    
    private static double getPente(PrecisePoint a, PrecisePoint b) {
        
        return (b.getY() - a.getY()) / (b.getX() - a.getX());
    }
    
    private static double getYPositiveValueOnCircle(double x) {
        
        return Math.sqrt(Math.abs(Math.pow(x, 2) - 1d));
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrecisePoint {
        
        private double x;
        private double y;
        
        public PrecisePoint(Point point) {
            
            this.x = point.getX();
            this.y = point.getY();
        }
        
        public Point toRoundedPoint() {
            
            return new Point((int) x, (int) y);
        }
    }
}
