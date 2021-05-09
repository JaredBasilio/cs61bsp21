package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Font;
import java.awt.Color;
import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.util.Arrays;

public class Engine {
    private static TERenderer ter = new TERenderer();

    /* Menu/StdDraw Items*/
    private static Font title;
    private static Font subtitle;
    private static Font display;
    private static String keystrokes;
    public static final ArrayList<Character> NUMBERS =
            new ArrayList<Character>(Arrays.asList('1', '2', '3', '4', '5',
                    '6', '7', '8', '9', '0'));
    private static final ArrayList<Character> LETTERS =
            new ArrayList<Character>(Arrays.asList('A', 'B', 'C', 'D', 'E',
                    'F', 'G', 'H', 'I', 'J', 'K',
                    'L', 'N', 'M', 'O', 'P', 'Q',
                    'R', 'S', 'T', 'U', 'V', 'W',
                    'X', 'Y', 'Z'));

    private static final File CWD = new File(System.getProperty("user.dir"));

    /* Feel free to change the width and height. */
    public static final int WIDTH = 120;
    public static final int HEIGHT = 70;

    private static TETile avatar;
    private static Random seed;
    private static String rawSeed;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    //TODO add coins and hud
    public void interactWithKeyboard() {
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        avatar = Tileset.AVATAR;
        title = new Font("Courier New", Font.BOLD, 30);
        subtitle = new Font("Segoe UI Symbol", Font.PLAIN, 17);
        keystrokes = "";
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
        createMenu();
        menuInteraction("X", null);
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        avatar = Tileset.AVATAR;
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        keystrokes = "";
        menuInteraction(input, finalWorldFrame);
        return finalWorldFrame;
    }

    /**
     * This method finds the bottoms most left room and returns the middle of that room for spawn
     *
     * @param rooms
     * @return bottom most left room
     */
    private static Position getSpawn(ArrayList<Room> rooms) {
        Double minDist = Double.POSITIVE_INFINITY;
        Position min = new Position(0, 0);
        Room r = new Room(1, 1, new Position(0, 0));
        for (int i = 0; i < rooms.size(); i++) {
            Room r2 = rooms.get(i);
            Position p2 = r2.getPosition();
            Double distance = Math.sqrt(Math.pow(p2.x, 2) + Math.pow(p2.y, 2));
            if (distance < minDist) {
                minDist = distance;
                min = p2;
                r = r2;
            }
        }
        return new Position(min.x + r.w / 2, min.y + r.h / 2);
    }

    /**
     * This method converts a String to a Long and sets the numerical seed to be Random
     *
     * @param input
     */
    private static void convertString(String input) {
        String raw = input;
        Long seedLong = 0L;
        for (int i = 0; i < raw.length(); i++) {
            seedLong += (long) Math.pow(10, raw.length() - i - 1) * (long) raw.charAt(i);
        }
        seed = new Random(seedLong);
    }

    /**
     * The World class is responsible for anything
     * world related such as creation, room generation etc.
     */
    public static class World {
        private static TETile[][] world;
        private static ArrayList<Room> rooms;
        private static HashSet<Position> floortiles;
        private static ArrayList<Ghost> ghosts;
        private static ArrayList<Position> graves;

        /**
         * creates the world through multiple steps ~
         * filling with nothing, filling with rooms and hallways.
         *
         * @param tiles
         * @return
         */
        World(TETile[][] tiles) {
            fillWorldWithNothing(tiles);
            generateWorld(tiles, seed);
        }

        /**
         * fills the world with nothing based on the height and width of the class
         *
         * @param w
         * @return
         */
        public static void fillWorldWithNothing(TETile[][] w) {
            for (int x = 0; x < WIDTH; x += 1) {
                for (int y = 0; y < HEIGHT; y += 1) {
                    w[x][y] = Tileset.NOTHING;
                }
            }
        }

        /**
         * generates the distinct rooms and hallways
         * based on the given seed, rooms are made then dispersed horizontally
         *
         * @param w
         * @param s
         * @return
         */
        public static void generateWorld(TETile[][] w, Random s) {
            int amount = RandomUtils.uniform(s, 25, 60);
            rooms = new ArrayList<Room>();
            floortiles = new HashSet<>();
            ghosts = new ArrayList<Ghost>();
            graves = new ArrayList<Position>();
            for (int i = 0; i < amount; i++) {
                int width = RandomUtils.uniform(s, 5, 10);
                int height = RandomUtils.uniform(s, 5, 10);
                int x = RandomUtils.uniform(s, WIDTH / 2 - 5, WIDTH / 2 + 5);
                int y = RandomUtils.uniform(s, 2, HEIGHT - 12);
                Position p = new Position(x, y);
                //valdates room position & size
                while (checkOverlap(w, p, width, height)) {
                    if (p.x < 2 || p.x + width + 2 > WIDTH) {
                        break;
                    }
                    if (p.x > WIDTH / 2) {
                        p.x += 1;
                    } else {
                        p.x -= 1;
                    }
                }
                if (checkOverlap(w, p, width, height)) {
                    continue;
                }
                Room room = new Room(width, height, p);
                room.buildRoom(w, floortiles);
                rooms.add(room);
            }
            connectRooms(w, rooms, floortiles);
            makeWalls(w, floortiles);
            for (int i = 0; i < 10; i++) { //creates ten coins
                makeObjects(w, "COIN");
            }
            for (int i = 0; i < 8; i++) {
                ghosts.add(makeObjects(w, "GHOST"));
            }
        }

        /**
         * generates the coins/ghosts in random places in the world
         * @param w
         */
        private static Ghost makeObjects(TETile[][] w, String type) {
            int x = RandomUtils.uniform(seed, WIDTH);
            int y = RandomUtils.uniform(seed, HEIGHT);
            if (w[x][y] == Tileset.FLOOR) {
                if (type.equals("COIN")) {
                    w[x][y] = Tileset.COIN;
                    return null;
                } else {
                    return new Ghost(x, y, w);
                }
            } else {
                return makeObjects(w, type);
            }
        }

        private static ArrayList<Ghost> getGhosts(){
            return ghosts;
        }

        private static void removeGhost(Ghost g) {
            ghosts.remove(g);
        }

        private static TETile[][] getWorld(){
            return world;
        }

        /**
         * connect rooms using prims algorithm
         *
         * @param w
         * @param r
         * @param f
         * @source https://www.youtube.com/watch?v=U9B39sDIupc
         */
        private static void connectRooms(TETile[][] w,
                                         ArrayList<Room> r, HashSet<Position> f) {
            ArrayList<Room> roomsCopy = new ArrayList<Room>(r);
            ArrayList<Room> path = new ArrayList<Room>();
            path.add(roomsCopy.remove(0)); // gets the first value

            while (roomsCopy.size() > 0) {
                Position temp = new Position(WIDTH / 2, HEIGHT / 2);
                double minDist = Double.POSITIVE_INFINITY;
                Room minP = new Room(0, 0, temp); //temp min_p
                Room p = new Room(0, 0, temp); //temp p

                for (Room room : path) {
                    Position p1 = room.getCenter();
                    for (Room room2 : roomsCopy) {
                        Position p2 = room2.getCenter();
                        double distance = Math.sqrt(Math.pow(p2.x - p1.x, 2)
                                + Math.pow(p2.y - p1.y, 2));
                        if (distance < minDist) {
                            minDist = distance;
                            minP = room2;
                            p = room;
                        }
                    }
                }
                path.add(minP);
                makeHallway(p.randomPos(seed),
                        path.get(path.size() - 1).randomPos(seed), seed, w, f);
                roomsCopy.remove(minP);
            }
        }

        /**
         * checks if the rooms will overlap with another room
         * if the room is placed at p, has a +2,-2 spacing for other rooms
         * @param a
         * @param p
         * @param w
         * @param h
         * @return
         */
        private static boolean checkOverlap(TETile[][] a, Position p, int w, int h) {
            for (int x = p.x; x < Math.min(w + p.x + 2, WIDTH); x += 1) { //checks ahead
                for (int y = p.y; y < Math.min(h + p.y + 2, HEIGHT); y += 1) {
                    if (a[x][y] != Tileset.NOTHING) {
                        return true;
                    }
                }
            }
            if (p.x > 2) { //checks behind 2 indices behind
                for (int x = p.x - 2; x < p.x; x += 1) {
                    for (int y = p.y; y < Math.min(h + p.y, HEIGHT); y += 1) {
                        if (a[x][y] != Tileset.NOTHING) {
                            return true;
                        }
                    }
                }
            }
            if (p.y > 2) { //checks 2 indices below
                for (int x = p.x; x < Math.min(w + p.x, WIDTH); x += 1) {
                    for (int y = p.y - 2; y < p.y; y += 1) {
                        if (a[x][y] != Tileset.NOTHING) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * connects the rooms using their position and floortiles
         *
         * @param p1
         * @param p2
         * @param s
         * @param w
         * @param f
         */
        private static void makeHallway(Position p1, Position p2, Random s, TETile[][] w,
                                        HashSet<Position> f) {
            int xcounter = p2.x - p1.x; // difference between two x coordinates
            int ycounter = p2.y - p1.y; // difference between two y coordinates,
            Position p3 = p1; //makes copy of p1
            int order = RandomUtils.uniform(s, 0, 2);
            int dx = sign(xcounter);
            int dy = sign(ycounter);
            xcounter = Math.abs(xcounter); // reinitializes to positive distance
            ycounter = Math.abs(ycounter); // reinitializes to positive distance
            if (order == 0) { //builds the horizontal hallway first
                while (xcounter > 0 && p3.x < WIDTH - 1 && p3.x >= 0) {
                    p3 = p3.shift(dx, 0);
                    if (p3.x < WIDTH - 1) { //the coordinate is within WIDTH bounds
                        //System.out.println(p3.x + " " + p3.y);
                        f.add(p3);
                        w[p3.x][p3.y] = Tileset.FLOOR;
                        xcounter--;
                    } else {
                        break;
                    }
                }
                while (ycounter > 0 && p3.y < HEIGHT - 1 && p3.y >= 0) {
                    p3 = p3.shift(0, dy);
                    if (p3.y < HEIGHT - 1) {
                        //System.out.println(p3.x + " " + p3.y);
                        f.add(p3);
                        w[p3.x][p3.y] = Tileset.FLOOR;
                        ycounter--;
                    } else {
                        break;
                    }

                }
            } else { //builds the vertical hallway first
                while (ycounter > 0 && p3.y < HEIGHT - 1 && p3.y >= 0) {
                    p3 = p3.shift(0, dy);
                    if (p3.y < HEIGHT - 1) {
                        //System.out.println(p3.x + " " + p3.y);
                        f.add(p3);
                        w[p3.x][p3.y] = Tileset.FLOOR;
                        ycounter--;
                    } else {
                        break;
                    }

                }
                while (xcounter > 0 && p3.x < WIDTH - 1 && p3.x >= 0) {
                    p3 = p3.shift(dx, 0);
                    if (p3.x < WIDTH - 1) { //the coordinate is within WIDTH bounds
                        //System.out.println(p3.x + " " + p3.y);
                        f.add(p3);
                        w[p3.x][p3.y] = Tileset.FLOOR;
                        xcounter--;
                    } else {
                        break;
                    }
                }
            }
        }

        /**
         * surrounds all floor tiles with walls unless already a floor tile
         * @param w
         * @param f
         */
        private static void makeWalls(TETile[][] w, HashSet<Position> f) {
            for (Position p : f) {
                if (!w[p.x + 1][p.y].equals(Tileset.FLOOR)) {
                    w[p.x + 1][p.y] = Tileset.WALL;
                }
                if (p.x + 1 < WIDTH && p.y + 1 < HEIGHT
                        && !w[p.x + 1][p.y + 1].equals(Tileset.FLOOR)) {
                    w[p.x + 1][p.y + 1] = Tileset.WALL;
                }
                if (p.x < WIDTH && p.y + 1 < HEIGHT && !w[p.x][p.y + 1].equals(Tileset.FLOOR)) {
                    w[p.x][p.y + 1] = Tileset.WALL;
                }
                if (!w[p.x - 1][p.y].equals(Tileset.FLOOR)) {
                    w[p.x - 1][p.y] = Tileset.WALL;
                }
                if (!w[p.x - 1][p.y - 1].equals(Tileset.FLOOR)) {
                    w[p.x - 1][p.y - 1] = Tileset.WALL;
                }
                if (!w[p.x][p.y - 1].equals(Tileset.FLOOR)) {
                    w[p.x][p.y - 1] = Tileset.WALL;
                }
                if (!w[p.x + 1][p.y - 1].equals(Tileset.FLOOR)) {
                    w[p.x + 1][p.y - 1] = Tileset.WALL;
                }
                if (p.y + 1 < HEIGHT && p.x >= 0
                        && !w[p.x - 1][p.y + 1].equals(Tileset.FLOOR)) {
                    w[p.x - 1][p.y + 1] = Tileset.WALL;
                }
            }
        }

        /**
         * gets arraylist of all rooms in the world
         *
         * @return
         */
        private static ArrayList<Room> getRooms() {
            return rooms;
        }

        /**
         * gets all floortiles of the world
         *
         * @return
         */
        private static HashSet<Position> getFloorTiles() {
            return floortiles;
        }

        public void addGrave(Position p) {
            graves.add(p);
        }

        public ArrayList<Position> getGraves() {
            return graves;
        }
    }


    /**
     * finds the sign of a number
     *
     * @param x
     * @return 1 is positive, -1 if negative
     */
    private static int sign(int x) {
        if (x > 0) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * position class for rooms
     */
    private static class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position shift(int dx, int dy) {
            return new Position(this.x + dx, this.y + dy);
        }
    }

    /**
     * rooms class for world
     */
    private static class Room {
        int w;
        int h;
        Position p;
        boolean connected;

        /**
         * world initialization
         *
         * @param width
         * @param height
         * @param position
         */
        Room(int width, int height, Position position) {
            w = width;
            h = height;
            p = position;
            connected = false;
        }

        /**
         * builds the room in world at the given floortiles
         *
         * @param world
         * @param floortiles
         */
        public void buildRoom(TETile[][] world, HashSet<Position> floortiles) {
            for (int x = p.x + 1; x < Math.min(w + p.x, WIDTH) - 1; x += 1) {
                for (int y = p.y + 1; y < Math.min(h + p.y, HEIGHT) - 1; y += 1) {
                    world[x][y] = Tileset.FLOOR;
                    floortiles.add(new Position(x, y));
                }
            }
        }

        /**
         * returns the position of the room
         *
         * @return
         */
        public Position getPosition() {
            return p;
        }

        /**
         * gets random position within the room
         * @param s
         * @return random position in room
         */
        public Position randomPos(Random s) {
            return new Position(RandomUtils.uniform(s, p.x + 1, Math.min(w + p.x, WIDTH) - 2),
                    RandomUtils.uniform(s, p.y + 1, Math.min(h + p.y, HEIGHT) - 2));

        }

        /**
         * gets the center of the room
         *
         * @return center of room
         */
        public Position getCenter() {
            return new Position(p.x + w / 2, p.y + h / 2);
        }
    }

    /**
     * creates the menu visuals
     */
    public static void createMenu() {
        StdDraw.setFont(title);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "CS 61B: THE GAME");
        StdDraw.setFont(subtitle);
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 2, "(N)ew Game");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 3, "(L)oad Last Game");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 4.2, "(R)eplay Last Game");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 5.4, "(C)hange Character");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 6.6, "(Q)uit");
        StdDraw.show();
    }

    /**
     * input seed mechanisms for input seed window
     *
     * @return final string
     */
    public static String inputSeed() {
        String inputSeed = "___________________";
        int index = 0;
        mainInputString(inputSeed);
        StdDraw.show();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                Character input = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (input == KeyEvent.VK_ESCAPE) {
                    StdDraw.clear(Color.black);
                    createMenu();
                    menuInteraction("X", null);
                } else if (index > 0 && input == KeyEvent.VK_BACK_SPACE) { //backspace
                    index -= 1;
                    inputSeed = replaceChar(index, inputSeed, '_');
                    mainInputString(inputSeed);
                } else if (index >= 0 && index <= inputSeed.length() && input == 'S') {
                    if (index == 0) { //nothing inputted
                        String randomSeed = "";
                        Random s = new Random();
                        for (int i = 0; i < 15; i++) {
                            Character c = NUMBERS.get(s.nextInt(NUMBERS.size()));
                            randomSeed += c;
                        }
                        return randomSeed;
                    }
                    return inputSeed.substring(0, index);
                } else if (index < inputSeed.length()
                        && (NUMBERS.contains(input) || LETTERS.contains(input))) {
                    inputSeed = replaceChar(index, inputSeed, input);
                    index += 1;
                    mainInputString(inputSeed);
                }
            }
        }
    }

    /**
     * replace the char of a string
     *
     * @param index
     * @param s
     * @param c
     * @return
     */
    private static String replaceChar(int index, String s, Character c) {
        return s.substring(0, index) + c + s.substring(index + 1);
    }

    /**
     * displays the string input screen
     *
     * @param inputSeed
     */
    private static void mainInputString(String inputSeed) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(title);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "CS 61B: THE GAME");
        StdDraw.setFont(new Font("Courier New", Font.PLAIN, 15));
        StdDraw.text(WIDTH / 2 - 4, HEIGHT / 2 - 2, "Input Seed:");
        StdDraw.textLeft(WIDTH / 2, HEIGHT / 2 - 2, inputSeed);
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 4, "Enter (S)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 5, "(An empty seed will produce a random seed)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 6, "YOUR LAST SAVE WILL BE OVERWRITTEN!");
        StdDraw.show();
    }

    /**
     * select the character screen
     *
     * @return
     */
    private static TETile characterSelect() {
        display = new Font("Segoe UI Symbol", Font.BOLD, 80);
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(title);
        int h = 0;
        int charNum = 5;
        ArrayList<TETile> characters = new ArrayList<TETile>();
        characters.add(Tileset.AVATAR);
        characters.add(Tileset.LAMBDA);
        characters.add(Tileset.SIGMA);
        characters.add(Tileset.ARROWRIGHT);
        characters.add(Tileset.TEEMO);
        ArrayList<String> names = new ArrayList<String>();
        names.add("Avatar");
        names.add("Lambda");
        names.add("Sigma");
        names.add("Arrow");
        names.add("DaTeemo");
        ArrayList<String> descriptions = new ArrayList<String>();
        descriptions.add("Other Names: at, address");
        descriptions.add("Other Names: EigenValue, CS 61A");
        descriptions.add("Other Names: Dude from overwatch, Sum of, Frat Symbol, σ");
        descriptions.add("Fun Fact: This Icon moves with Direction!");
        descriptions.add("I'mma turn a ghost into a convertible. Lets gooooo.");
        displayOptions(characters, h);
        StdDraw.textLeft((double) WIDTH * 2 / 3 - 7,
                HEIGHT / 2 + 6, "Character Selected: " + names.get(h));
        StdDraw.setFont(subtitle);
        StdDraw.textLeft(WIDTH * 2 / 3 - 7, HEIGHT * 1 / 2 - 7, descriptions.get(h));
        StdDraw.rectangle((double) WIDTH * 2 / 3 - 0.1, HEIGHT / 2 - 0.2, 7, 5);
        StdDraw.setFont(display);
        StdDraw.text(WIDTH * 2 / 3,
                HEIGHT * 1 / 2, Character.toString(characters.get(h).character()));
        StdDraw.show();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                Character input = StdDraw.nextKeyTyped();
                if (Character.toUpperCase(input) == 'S') {
                    if (h == 0) {
                        h = characters.size() - 1;
                    } else {
                        h -= 1;
                    }
                }
                if (Character.toUpperCase(input) == 'W') {
                    if (h == charNum - 1) {
                        h = 0;
                    } else {
                        h += 1;
                    }
                }
                if (Character.toUpperCase(input) == KeyEvent.VK_ENTER) {
                    return characters.get(h);
                }
                StdDraw.clear(Color.black);
                displayOptions(characters, h);
                StdDraw.textLeft((double) WIDTH * 2 / 3 - 7,
                        HEIGHT / 2 + 6, "Character Selected: " + names.get(h));
                StdDraw.textLeft(WIDTH * 2 / 3 - 7, HEIGHT * 1 / 2 - 7, descriptions.get(h));
                StdDraw.setFont(display);
                StdDraw.rectangle((double) WIDTH * 2 / 3 - 0.1, HEIGHT / 2 - 0.2, 7, 5);
                StdDraw.text(WIDTH * 2 / 3, HEIGHT * 1 / 2,
                        Character.toString(characters.get(h).character()));
                StdDraw.show();
            }
        }
    }

    /**
     * displays the options for a character
     *
     * @param options
     * @param index
     */
    private static void displayOptions(ArrayList<TETile> options, int index) {
        StdDraw.setFont(subtitle);
        int h = HEIGHT * 3 / 8;
        for (int i = 0; i < options.size(); i++) {
            if (i == index) {
                StdDraw.rectangle((double) WIDTH / 3 - 0.5, h, 1.5, 1.5);
            }
            StdDraw.text(WIDTH / 3, h, Character.toString(options.get(i).character()));
            h += HEIGHT / 8;
        }
    }

    /**
     * loads the last save
     *
     * @return
     */
    private static String lastSave() {
        if (!Utils.join(CWD, "gameSave.txt").exists()) {
            System.exit(0);
            //return null;
        }
        String inputs = Utils.readContentsAsString(Utils.join(CWD, "gameSave.txt"));
        return inputs;
    }

    private static void menuInteraction(String inputString, TETile[][] inputworld) {
        if (inputString.equals("X")) {
            while (true) {
                Character input = ' ';
                if (StdDraw.hasNextKeyTyped()) {
                    input = StdDraw.nextKeyTyped();
                }
                if (Character.toUpperCase(input) == 'L'
                        || Character.toUpperCase(input) == 'R') {

                    String directions = loadCommand();
                    TETile[][] w = new TETile[WIDTH][HEIGHT];
                    World worldObject = new World(w);
                    if (Character.toUpperCase(input) == 'R') {
                        worldInteraction(worldObject, w, directions, true, false);
                    } else {
                        worldInteraction(worldObject, w, directions, false, false);
                    }
                }
                if (Character.toUpperCase(input) == 'N') {
                    String directions = null;
                    newCommand();
                    TETile[][] w = new TETile[WIDTH][HEIGHT];
                    World worldObject = new World(w);
                    worldInteraction(worldObject, w, directions, false, false);
                }
                if (Character.toUpperCase(input) == 'C') {
                    avatar = characterSelect();
                    StdDraw.clear(Color.black);
                    createMenu();
                    menuInteraction("X", null);
                }
                if (Character.toUpperCase(input) == 'Q') {
                    System.exit(0);
                }

            }
        } else {
            if (Character.toUpperCase(inputString.charAt(0)) == 'L') {
                keystrokes += 'L';
                rawSeed = "";
                String loadinputs = lastSave();
                int counter = 1;
                while (counter < loadinputs.length()) {
                    counter++;
                    if (loadinputs.charAt(counter - 1) == 'S') {
                        break;
                    } else {
                        rawSeed = rawSeed + loadinputs.charAt(counter - 1);
                    }
                }
                keystrokes = keystrokes + rawSeed + 'S';
                String directions = loadinputs.substring(counter, loadinputs.length() - 2);
                directions += inputString.substring(1);
                convertString(rawSeed);
                TETile[][] w = inputworld;
                worldInteraction(new World(w), w, directions, false, true);

            }
            if (Character.toUpperCase(inputString.charAt(0)) == 'N') {
                keystrokes += 'N';
                rawSeed = "";
                int counter = 1;
                while (counter < inputString.length()) {
                    counter++;
                    if (Character.toUpperCase(inputString.charAt(counter - 1)) == 'S') {
                        break;
                    } else {
                        rawSeed = rawSeed + inputString.charAt(counter - 1);
                    }
                }
                keystrokes = keystrokes + rawSeed + 'S';
                String directions = inputString.substring(counter);
                convertString(rawSeed);
                TETile[][] w = inputworld;
                worldInteraction(new World(w), w, directions, false, true);
            }
        }
    }


    /**
     * saves the the keystrokes and seeds to be loaded by
     * interact with input string and then can be interacted with
     */
    private static void saveFile() {
        File dir = new File(System.getProperty("user.dir"));
        Utils.writeContents(Utils.join(dir, "gameSave.txt"), keystrokes);
    }

    private static void moveGhosts(World worldObject, TETile[][] w) {
        ArrayList<Ghost> ghosts = worldObject.getGhosts();
        ArrayList<Ghost> killedGhosts = new ArrayList<>();
        for (Ghost g : ghosts) {
            g.moveRand(w);
            if (!g.alive) {
                killedGhosts.add(g);
            }
        }
        for (Ghost g: killedGhosts) {
            worldObject.removeGhost(g);
            worldObject.addGrave(new Position(g.x,g.y));
        }
    }

    private static void worldInteraction(World worldObject, TETile[][] world,
                                         String input, boolean replay, boolean inputstring) {
        Position p = getSpawn(worldObject.getRooms());
        ArrayList<Position> trail = new ArrayList<>();
        int trailcounter = 20;
        if (!inputstring) {
            ter.initialize(WIDTH, HEIGHT);
        }
        int x = p.x;
        int y = p.y;
        int score = 0;
        int moves = 150;
        ArrayList<TETile> arrows = new ArrayList<TETile>();
        if (avatar == Tileset.ARROWRIGHT) { //initializes a multi directional character
            arrows.addAll(Arrays.asList(Tileset.ARROWRIGHT, Tileset.ARROWLEFT,
                    Tileset.ARROWUP, Tileset.ARROWDOWN));
        }
        world[x][y] = avatar;
        boolean awaitingQ = false;

        mouseMovement(score, inputstring, world);
        String useinputs = input;
        while (true) {
            Character ctrl = null;
            if (input != null && useinputs.length() > 0) { //considers test for input with string
                ctrl = useinputs.charAt(0);
                useinputs = useinputs.substring(1);
            } else if (!inputstring && StdDraw.hasNextKeyTyped()) {
                ctrl = StdDraw.nextKeyTyped();
            }
            if (ctrl != null) {
                world[x][y] = Tileset.FLOOR;
                int oldx = x;
                int oldy = y;
                if (Character.toUpperCase(ctrl) == ':') {
                    keystrokes += ':';
                    awaitingQ = true;
                    world[x][y] = avatar;
                    if (inputstring) {
                        keystrokes += 'Q';
                        saveFile();
                        System.exit(0);
                    }
                    continue;
                }
                if (Character.toUpperCase(ctrl) == 'Q' && awaitingQ) {
                    keystrokes += 'Q';
                    saveFile();
                    System.exit(0);
                }

                if (Character.toUpperCase(ctrl) == 'W') {
                    keystrokes += 'W';
                    if (world[x][y + 1] != Tileset.WALL) {
                        y += 1;
                        switchAvatar(arrows, 2);
                    }
                }
                if (Character.toUpperCase(ctrl) == 'A') {
                    keystrokes += 'A';
                    if (world[x - 1][y] != Tileset.WALL) {
                        x -= 1;
                        switchAvatar(arrows, 1);
                    }
                }
                if (Character.toUpperCase(ctrl) == 'S') {
                    keystrokes += 'S';
                    if (world[x][y - 1] != Tileset.WALL) {
                        y -= 1;
                        switchAvatar(arrows, 3);
                    }
                }
                if (Character.toUpperCase(ctrl) == 'D') {
                    keystrokes += 'D';
                    if (world[x + 1][y] != Tileset.WALL) {
                        x += 1;
                        switchAvatar(arrows, 0);
                    }
                }
                if (world[x][y] == Tileset.COIN) {
                    score += 1;
                    moves += 10;
                }
                if (trailcounter < 0) {
                    Position expired = trail.remove(0);
                    world[expired.x][expired.y] = Tileset.FLOOR;
                    for (Position pos : trail) {
                        world[pos.x][pos.y] = Tileset.SHROOM;
                    }
                }
                checkLife(world[x][y]);
                if (Character.toUpperCase(ctrl) == 'W'
                        || Character.toUpperCase(ctrl) == 'A'
                        || Character.toUpperCase(ctrl) == 'S'
                        || Character.toUpperCase(ctrl) == 'D') {
                    world[oldx][oldy] = Tileset.SHROOM;
                    trail.add(new Position(oldx, oldy));
                    trailcounter-= 1;
                    moveGhosts(worldObject, world);
                    moves--;
                }
                generateGraves(worldObject, world);
                world[x][y] = avatar;
            }

            if (score >= 10) {
                endScreen("Won");
            }
            if (moves <= 0) {
                endScreen("ran out of moves");
            }
            if (inputstring && useinputs.isEmpty()) {
                return;
            } else if (!inputstring) {
                displayWorld(world, replay, useinputs, input, score, moves);
            }
        }
    }

    private static void generateGraves(World worldObject, TETile[][] world) {
        for (Position p: worldObject.getGraves()) {
            world[p.x][p.y] = Tileset.GRAVE;
        }
    }

    private static void checkLife(TETile a) {
        if (a == Tileset.GHOST) {
            endScreen("Died");
        }
    }

    private static void createHud(int score) {
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(subtitle);
        StdDraw.textRight(WIDTH - 1, HEIGHT - 1, "Score: " + score);
        StdDraw.textRight(WIDTH - 1, HEIGHT - 2, "Seed: " + rawSeed);
    }

    private static void switchAvatar(ArrayList<TETile> arrows, int i) {
        if (!arrows.isEmpty()) {
            avatar = arrows.get(i);
        }
    }

    private static void displayWorld(TETile[][] world, boolean replay,
                                     String useinputs, String input, int score, int movesleft) {
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(subtitle);
        StdDraw.textRight(WIDTH - 1, HEIGHT - 1, "Score: " + score);
        StdDraw.textRight(WIDTH - 1, HEIGHT - 2, "Seed: " + rawSeed);
        StdDraw.textRight(WIDTH - 1, HEIGHT - 3, "Moves: " + movesleft);
        if ((int) StdDraw.mouseX() >= 0 && (int) StdDraw.mouseX() < WIDTH
                && (int) StdDraw.mouseY() >= 0 && (int) StdDraw.mouseY() < HEIGHT) {
            StdDraw.textLeft(1, HEIGHT - 1,
                    world[(int) StdDraw.mouseX()][(int) StdDraw.mouseY()].description());
        }
        if (input == null || useinputs.length() == 0) {
            StdDraw.show();
            ter.renderFrame(world);
        } else if (replay && useinputs.length() > 0) {
            ter.renderFrame(world);
            StdDraw.pause(500);
        }
    }

    private static class Ghost {
        int x;
        int y;
        TETile prevTile;
        boolean alive;

        Ghost(int x, int y, TETile[][] world) {
            this.x = x;
            this.y = y;
            prevTile = world[this.x][this.y];
            world[this.x][this.y] = Tileset.GHOST;
            alive = true;
        }

        public void moveRand(TETile[][] world) {
            boolean foundNewPos = false;
            while (!foundNewPos) {
                int val = RandomUtils.uniform(seed, 4);
                if (val == 0 && world[x + 1][y]
                        != Tileset.WALL && world[x + 1][y] != Tileset.GHOST) {
                    world[x][y] = prevTile;
                    this.x += 1;
                    foundNewPos = true;
                } else if (val == 1 && world[x - 1][y]
                        != Tileset.WALL && world[x - 1][y] != Tileset.GHOST) {
                    world[x][y] = prevTile;
                    this.x -= 1;
                    foundNewPos = true;
                } else if (val == 2 && world[x][y + 1]
                        != Tileset.WALL && world[x][y + 1] != Tileset.GHOST) {
                    world[x][y] = prevTile;
                    this.y += 1;
                    foundNewPos = true;
                } else if (val == 3 && world[x][y - 1]
                        != Tileset.WALL && world[x][y - 1] != Tileset.GHOST) {
                    world[x][y] = prevTile;
                    this.y -= 1;
                    foundNewPos = true;
                }
            }
            prevTile = world[this.x][this.y];
            if (prevTile == Tileset.SHROOM) {
                alive = false;
            } else {
                world[this.x][this.y] = Tileset.GHOST;
            }
        }

    }

    private static void mouseMovement(int score, boolean inputstring, TETile [][] world) {
        if (!inputstring) {
            createHud(score);
            if ((int) StdDraw.mouseX() >= 0
                    && (int) StdDraw.mouseX() < WIDTH && (int) StdDraw.mouseY() >= 0
                    && (int) StdDraw.mouseY() < HEIGHT) {
                StdDraw.textLeft(1, HEIGHT - 1,
                        world[(int) StdDraw.mouseX()][(int) StdDraw.mouseY()].description());
            }
            StdDraw.show();
        }
    }

    private static void newCommand() {
        keystrokes += 'N';
        StdDraw.clear(Color.BLACK); //clears for overlapping
        rawSeed = inputSeed();
        keystrokes += rawSeed;
        keystrokes += 'S';
        convertString(rawSeed);
    }

    private static String loadCommand() {
        keystrokes += 'L';
        rawSeed = "";
        String loadinputs = lastSave();
        System.out.println(loadinputs);
        int counter = 1;
        while (counter < loadinputs.length()) {
            counter++;
            if (loadinputs.charAt(counter - 1) == 'S') {
                break;
            } else {
                rawSeed = rawSeed + loadinputs.charAt(counter - 1);
            }
        }
        keystrokes += rawSeed;
        keystrokes += 'S';
        convertString(rawSeed);
        return loadinputs.substring(counter, loadinputs.length() - 2);
    }

    private static void endScreen(String message) {
        StdDraw.clear(Color.black);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "You " + message );
        StdDraw.text(WIDTH / 2 - 3, HEIGHT / 2 - 3, "(R)etry");
        StdDraw.text(WIDTH / 2 + 3, HEIGHT / 2 - 3, "(Q)uit");
        StdDraw.show();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                Character i = StdDraw.nextKeyTyped();
                if (Character.toUpperCase(i) == 'R') {
                    TETile[][] w = new TETile[WIDTH][HEIGHT];
                    convertString(rawSeed);
                    World worldObject = new World(w);
                    worldInteraction(worldObject, w, null, false, false);
                }
                if (Character.toUpperCase(i) == 'Q') {
                    StdDraw.clear(Color.BLACK);
                    createMenu();
                    menuInteraction("X", null);
                }
            }
        }
    }
}

