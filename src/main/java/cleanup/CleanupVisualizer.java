package cleanup;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.visualizer.OOStatePainter;
import burlap.visualizer.ObjectPainter;
import burlap.visualizer.StateRenderLayer;
import burlap.visualizer.Visualizer;
import cleanup.state.CleanupState;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CleanupVisualizer {

    public static String imagePath = "./data/resources/robotImages/";

    private CleanupVisualizer() {
        // do nothing
    }

    public static Visualizer getVisualizer(int width, int height) {
        Visualizer v = new Visualizer(getStateRenderLayer(width, height));
        return v;
    }

    public static StateRenderLayer getStateRenderLayer(int width, int height) {

        StateRenderLayer srl = new StateRenderLayer();

        OOStatePainter oopainter = new OOStatePainter();
        srl.addStatePainter(oopainter);


        oopainter.addObjectClassPainter(Cleanup.CLASS_ROOM, new RoomPainter(0, 0, width, height));
        oopainter.addObjectClassPainter(Cleanup.CLASS_DOOR, new DoorPainter(0, 0, width, height));
        oopainter.addObjectClassPainter(Cleanup.CLASS_BLOCK, new BlockPainter(width, height));
        oopainter.addObjectClassPainter(Cleanup.CLASS_AGENT, new AgentPainter(width, height));

        return srl;
    }

    public static Visualizer getVisualizer(Cleanup domainGenerator) {
        Visualizer v = new Visualizer(getStateRenderLayer(domainGenerator.getWidth(), domainGenerator.getHeight()));
        return v;
    }

    public static class AgentPainter implements ObjectPainter, ImageObserver {

        public int minx = 0;
        public int miny = 0;

        public int maxx;
        public int maxy;
        public HashMap<String, BufferedImage> dirToImage;

        public AgentPainter(int maxx, int maxy) {
            this.maxx = maxx;
            this.maxy = maxy;


            dirToImage = new HashMap<String, BufferedImage>(4);
            try {
                dirToImage.put("north", ImageIO.read(new File(imagePath + "robotNorth.png")));
                dirToImage.put("south", ImageIO.read(new File(imagePath + "robotSouth.png")));
                dirToImage.put("east", ImageIO.read(new File(imagePath + "robotEast.png")));
                dirToImage.put("west", ImageIO.read(new File(imagePath + "robotWest.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob,
                                float cWidth, float cHeight) {

            g2.setColor(Color.blue);

            float domainXScale = (maxx) - minx;
            float domainYScale = (maxy) - miny;

            //determine then normalized width
            float width = (1.0f / domainXScale) * cWidth;
            float height = (1.0f / domainYScale) * cHeight;

            int x = (Integer) ob.get(Cleanup.ATT_X);
            int y = (Integer) ob.get(Cleanup.ATT_Y);

            float rx = x * width;
            float ry = cHeight - height - y * height;

            String dir = ob.get(Cleanup.ATT_DIR).toString();

            BufferedImage img = this.dirToImage.get(dir);
            g2.drawImage(img, (int) rx, (int) ry, (int) width, (int) height, this);


        }

        @Override
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            // TODO Auto-generated method stub
            return false;
        }


    }

    public static class BlockPainter implements ObjectPainter, ImageObserver {

        public int minx = 0;
        public int miny = 0;

        public int maxx;
        public int maxy;

        protected Map<String, BufferedImage> shapeAndColToImages;

        public BlockPainter(int maxx, int maxy) {
            this.maxx = maxx;
            this.maxy = maxy;
            this.shapeAndColToImages = new HashMap<String, BufferedImage>();
            this.initImages(imagePath);
        }

        protected void initImages(String pathToImageDir) {
            if (!pathToImageDir.endsWith("/")) {
                pathToImageDir = pathToImageDir + "/";
            }
            for (String shapeName : Cleanup.SHAPES) {
                for (String colName : Cleanup.COLORS_BLOCKS) {
                    String key = this.shapeKey(shapeName, colName);
                    String path = pathToImageDir + shapeName + "/" + key + ".png";
                    try {
                        BufferedImage img = ImageIO.read(new File(path));
                        this.shapeAndColToImages.put(key, img);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
            }
        }

        @Override
        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob,
                                float cWidth, float cHeight) {

            g2.setColor(colorForName(ob.get(Cleanup.ATT_COLOR).toString()));

            float domainXScale = (maxx) - minx;
            float domainYScale = (maxy) - miny;


            //determine then normalized width
            float width = (1.0f / domainXScale) * cWidth;
            float height = (1.0f / domainYScale) * cHeight;

            int x = (Integer) ob.get(Cleanup.ATT_X);
            int y = (Integer) ob.get(Cleanup.ATT_Y);

            float rx = x * width;
            float ry = cHeight - height - y * height;

            String colName = ob.get(Cleanup.ATT_COLOR).toString();
            String shapeName = ob.get(Cleanup.ATT_SHAPE).toString();
            String key = this.shapeKey(shapeName, colName);
            BufferedImage img = this.shapeAndColToImages.get(key);
            if (img == null) {
                Color col = colorForName(ob.get(Cleanup.ATT_COLOR).toString()).darker();

                g2.setColor(col);
                g2.fill(new Rectangle2D.Float(rx, ry, width, height));

            } else {
                g2.drawImage(img, (int) rx, (int) ry, (int) width, (int) height, this);
            }

        }

        protected String shapeKey(String shape, String color) {
            return shape + this.firstLetterCapped(color);
        }

        protected String firstLetterCapped(String input) {
            return input.substring(0, 1).toUpperCase() + input.substring(1);
        }

        @Override
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            return false;
        }


    }


    public static class RoomPainter implements ObjectPainter {

        protected int minX = -1;
        protected int minY = -1;
        protected int maxX = -1;
        protected int maxY = -1;

        public RoomPainter(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }


        @Override
        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {

            CleanupState cws = (CleanupState) s;
            float domainXScale = Cleanup.maxRoomXExtent(cws) + 1f;
            float domainYScale = Cleanup.maxRoomYExtent(cws) + 1f;

            if (maxX != -1) {
                domainXScale = maxX;
                domainYScale = maxY;
            }

            //determine then normalized width
            float width = (1.0f / domainXScale) * cWidth;
            float height = (1.0f / domainYScale) * cHeight;

            int top = (Integer) ob.get(Cleanup.ATT_TOP);
            int left = (Integer) ob.get(Cleanup.ATT_LEFT);
            int bottom = (Integer) ob.get(Cleanup.ATT_BOTTOM);
            int right = (Integer) ob.get(Cleanup.ATT_RIGHT);

            Color rcol = colorForName(ob.get(Cleanup.ATT_COLOR).toString());
            float[] hsb = new float[3];
            Color.RGBtoHSB(rcol.getRed(), rcol.getGreen(), rcol.getBlue(), hsb);
            hsb[1] = 0.4f;
            rcol = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);

            for (int i = left; i <= right; i++) {
                for (int j = bottom; j <= top; j++) {

                    float rx = i * width;
                    float ry = cHeight - height - j * height;

                    if (i == left || i == right || j == bottom || j == top) {
                        if (cws.doorContainingPoint(i, j) == null) {
                            g2.setColor(Color.black);
                            g2.fill(new Rectangle2D.Float(rx, ry, width, height));
                        }
                    } else {
                        g2.setColor(rcol);
                        g2.fill(new Rectangle2D.Float(rx, ry, width, height));
                    }
                }
            }

        }

    }


    public static class DoorPainter implements ObjectPainter {


        protected int minX = -1;
        protected int minY = -1;
        protected int maxX = -1;
        protected int maxY = -1;

        public DoorPainter(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }


        @Override
        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {
            float domainXScale = Cleanup.maxRoomXExtent(s) + 1f;
            float domainYScale = Cleanup.maxRoomYExtent(s) + 1f;

            if (maxX != -1) {
                domainXScale = maxX;
                domainYScale = maxY;
            }

            //determine then normalized width
            float width = (1.0f / domainXScale) * cWidth;
            float height = (1.0f / domainYScale) * cHeight;

            int top = (Integer) ob.get(Cleanup.ATT_TOP);
            int left = (Integer) ob.get(Cleanup.ATT_LEFT);
            int bottom = (Integer) ob.get(Cleanup.ATT_BOTTOM);
            int right = (Integer) ob.get(Cleanup.ATT_RIGHT);

            g2.setColor(Color.white);

            String lockedState = ob.get(Cleanup.ATT_LOCKED).toString();
            if (lockedState.equals("unknown")) {
                g2.setColor(Color.gray);
            } else if (lockedState.equals("locked")) {
                g2.setColor(Color.black);
            }


            for (int i = left; i <= right; i++) {
                for (int j = bottom; j <= top; j++) {

                    float rx = i * width;
                    float ry = cHeight - height - j * height;
                    g2.fill(new Rectangle2D.Float(rx, ry, width, height));

                }
            }
        }
    }

    protected static Color colorForName(String colName) {

        Color col = Color.darkGray; //default color

        Field field;
        try {
            field = Class.forName("java.awt.Color").getField(colName);
            col = (Color) field.get(null);

        } catch (Exception e) {
        }

        return col;
    }


}
