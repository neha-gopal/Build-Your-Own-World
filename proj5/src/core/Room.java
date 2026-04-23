package core;

public class Room {
    private int x;      // bottom-left x coordinate
    private int y;      // bottom-left y coordinate
    private int width;
    private int height;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public int centerX() {
        return x + width / 2;
    }

    public int centerY() {
        return y + height / 2;
    }

    public boolean overlaps(Room other) {
        return !(this.x + this.width +2 <= other.x -3 ||
                other.x + other.width+2  <= this.x -3||
                this.y + this.height +2  <= other.y -3||
                other.y + other.height +2<= this.y -3);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

}
