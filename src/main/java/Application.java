import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.input.KeyStroke;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
Color Pallet:
background: #3f434a
walls:      #1d1f24
hero:       #ffffff
monsters:   #ff0000
coins:      #d4a94e
*/

public class Application {
    public static void main(String[] args) {
        try {
            Game game = new Game(40, 20, 1);
            game.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Game {
    private Screen screen;
    private Arena arena;

    public Game (int width, int height, int scale) throws IOException {
        width -= (width % scale); height -= (height % scale);
        TerminalSize terminalSize = new TerminalSize((width + 2) * scale,(height + 4) * scale);
        arena = new Arena(width, height, scale);
        DefaultTerminalFactory terminalFactory = new
                DefaultTerminalFactory().setInitialTerminalSize(terminalSize);
        Terminal terminal = terminalFactory.createTerminal();
        screen = new TerminalScreen(terminal);
        screen.setCursorPosition(null); // we don't need a cursor
        screen.startScreen(); // screens must be started
        screen.doResizeIfNecessary(); // resize screen if necessary
    }

    public void run() throws IOException {
        while(true) {
            draw();
            KeyStroke key = screen.readInput();
            if(!processKey(key)) break;
            if(arena.verifyMonsterCollisions()) break;
        }
        screen.close();
    }

    private void draw() throws IOException {
        screen.clear();
        arena.draw(screen.newTextGraphics());
        screen.refresh();
    }

    private boolean processKey(KeyStroke key) {
        return arena.processKey(key);
    }
}

class Arena {
    private int width, height, scale;
    private List<Wall> cosmeticWalls;
    private List<Wall> walls;
    private List<Coin> coins;
    private List<Monster> monsters;
    private Hero hero;
    public Arena(int width, int height, int scale) {
        this.width = width;
        this.height = height;
        this.scale = scale;
        hero = new Hero(10, 10);
        walls = createWalls();
        cosmeticWalls = createCosmeticWalls();
        coins = createCoins();
        monsters = createMonsters();
    }

    public boolean processKey(KeyStroke key) {
        switch(key.getKeyType()) {
            case ArrowUp:
                moveHero(hero.moveUp());
                break;
            case ArrowDown:
                moveHero(hero.moveDown());
                break;
            case ArrowLeft:
                moveHero(hero.moveLeft());
                break;
            case ArrowRight:
                moveHero(hero.moveRight());
                break;
            case Character:
                if (key.getCharacter() == 'q')
                    return false;
                break;
            case EOF:
                return false;
        }
        return true;
    }
    private void moveHero(Position pos) {
        if (canMoveTo(pos)) {
            hero.setPos(pos);
            for (int i = 0; i < coins.size(); i++)
                if (pos.equals((coins.get(i)).getPos())){
                    retrieveCoins(i);
                    break;
                }
            moveMonsters();
        }
    }
    private boolean canMoveTo(Position pos) {
        if (pos.getX() < 0) return false;
        if (pos.getX() >= width) return false;
        if (pos.getY() < 0) return false;
        if (pos.getY() >= height) return false;
        for (Wall wall : walls)
            if (pos.equals(wall.getPos()))
                return false;
        return true;
    }
    public void draw(TextGraphics graphics) {
        graphics.setBackgroundColor(TextColor.Factory.fromString("#3f434a"));
        graphics.fillRectangle(new TerminalPosition(scale,scale), new TerminalSize(width * scale, (height + 3) * scale), ' ');
        hero.draw(graphics, scale);
        hero.drawHealthAndScore(graphics, width, height, scale);
        for(Wall wall : cosmeticWalls) wall.draw(graphics, scale);
        for(Wall wall : walls) wall.draw(graphics, scale);
        for(Coin coin : coins) coin.draw(graphics, scale);
        for(Monster monster : monsters) monster.draw(graphics, scale);
    }

    private List<Wall> createWalls(){ return new ArrayList<>(); }

    private List<Wall> createCosmeticWalls() {
        List<Wall> walls = new ArrayList<>();
        for(int c = 0; c < width; c++) {
            walls.add(new Wall(c, -1));
            walls.add(new Wall(c, height));
            walls.add(new Wall(c, height + 2));
        }
        for(int r = -1; r < height + 3; r++) {
            walls.add(new Wall(-1, r));
            walls.add(new Wall(width, r));
        }
        return walls;
    }

    private List<Coin> createCoins() {
        Random random = new Random();
        ArrayList<Coin> coins = new ArrayList<>();
        Position pos;
        boolean canAdd;
        for(int i = 0; i < 5;) {
            pos = new Position(random.nextInt(width - 2) + 1, random.nextInt(height - 2) + 1);
            canAdd = true;
            for(Coin coin : coins)
                if(pos.equals(coin.getPos())) {
                    canAdd = false;
                    break;
                }
            if(pos.equals(hero.getPos())) canAdd = false;
            if(canAdd) {
                coins.add(new Coin(pos.getX(), pos.getY()));
                i++;
            }
        }
        return coins;
    }

    private void retrieveCoins(int index){
        coins.remove(index);
        hero.addScore();
        if (coins.size() == 0) {
            coins = createCoins();
            addMonster();
        }
    }

    private List<Monster> createMonsters() {
        Random random = new Random();
        ArrayList<Monster> monsters = new ArrayList<>();
        Position pos;
        boolean canAdd;
        for(int i = 0; i < 5;) {
            pos = new Position(random.nextInt(width - 2) + 1, random.nextInt(height - 2) + 1);
            canAdd = true;
            for(Monster monster : monsters)
                if(pos.equals(monster.getPos())) {
                    canAdd = false;
                    break;
                }
            if(pos.equals(hero.getPos())) canAdd = false;
            if(canAdd) {
                monsters.add(new Monster(pos.getX(), pos.getY()));
                i++;
            }
        }
        return monsters;
    }

    public void addMonster() {
        Random random = new Random();
        Position pos;
        boolean canAdd = false;
        while (!canAdd) {
            pos = new Position(random.nextInt(width - 2) + 1, random.nextInt(height - 2) + 1);
            canAdd = true;
            for (Monster monster : monsters)
                if (pos.equals(monster.getPos())) {
                    canAdd = false;
                    break;
                }
            if (pos.equals(hero.getPos())) canAdd = false;
            if (canAdd) {
                monsters.add(new Monster(pos.getX(), pos.getY()));
            }
        }
    }

    public void moveMonsters() {
        for(Monster monster : monsters) {
            Position pos = monster.move();
            if(canMoveTo(pos)) monster.setPos(pos);
        }
    }

    public boolean verifyMonsterCollisions() {
        for(Monster monster : monsters)
            if(monster.getPos().equals(hero.getPos())) {
                if(hero.damaged(40)){
                    System.out.println("You lost! You caught " + hero.getScore() + " coins.");
                    return true;
                }
            }
        return false;
    }
}

class Element {
    protected Position pos;
    public Element(int x, int y) { pos = new Position(x, y); }

    public Position getPos() { return pos; }
    public void setPos(Position pos) { this.pos = pos; }

    public void draw(TextGraphics graphics, String str, String color, int scale) {
        graphics.setForegroundColor(TextColor.Factory.fromString(color));
        graphics.enableModifiers(SGR.BOLD);
        str = str.repeat(scale);
        for(int i = 0; i < scale; i++)
            graphics.putString(new TerminalPosition((pos.getX() + 1) * scale, (pos.getY() + 1) * scale + i), str);
    }
}

class Hero extends Element {
    private int health = 100, score = 0;
    public Hero(int x, int y) { super(x, y); }

    public int getScore() { return score; }

    public Position moveUp() { return new Position(pos.getX(), pos.getY() - 1); }
    public Position moveDown() { return new Position(pos.getX(), pos.getY() + 1); }
    public Position moveLeft() { return new Position(pos.getX() - 1, pos.getY()); }
    public Position moveRight() { return new Position(pos.getX() + 1, pos.getY()); }

    public void draw(TextGraphics graphics, int scale){
        super.draw(graphics, "X", "#ffffff", scale);
    }
    public void drawHealthAndScore(TextGraphics graphics, int width, int height, int scale){
        String str = "+";
        str = str.repeat(health * (width * scale - 3) / 100);
        String color = "#00ff00";
        if (health <= 20) color = "#ff0000";
        else if (health <= 60) color = "#ffff00";
        graphics.setForegroundColor(TextColor.Factory.fromString(color));
        for (int i = 0; i < scale; i++)
            graphics.putString(new TerminalPosition(scale, (height + 2) * scale + i), str);
        graphics.setForegroundColor(TextColor.Factory.fromString("#ffff00"));
        graphics.putString(new TerminalPosition((width + 1) * scale - 3,(height + 3) * scale - 1), String.format("%03d", score));
    }
    public boolean damaged(int amount){
        health -= amount;
        return health == 0;
    }
    public void addScore() { score++; }
}

class Wall extends Element {
    public Wall(int x, int y) { super(x, y); }

    public void draw(TextGraphics graphics, int scale) {
        super.draw(graphics, "O", "#1d1f24", scale);
    }
}

class Coin extends Element {
    public Coin(int x, int y) { super(x, y); }

    public void draw(TextGraphics graphics, int scale) {
        super.draw(graphics, "*", "#d4a94e", scale);
    }
}

class Monster extends Element {
    public Monster(int x, int y) { super(x, y); }

    public Position move() {
        Random random = new Random();
        switch(random.nextInt(4)) {
            case 0: // UP
                return new Position(pos.getX(), pos.getY() - 1);
            case 1: // DOWN
                return new Position(pos.getX(), pos.getY() + 1);
            case 2: // LEFT
                return new Position(pos.getX() - 1, pos.getY());
            case 3: // RIGHT
                return new Position(pos.getX() + 1, pos.getY());
            default:
                return new Position( pos.getX(), pos.getY());
        }
    }

    public void draw(TextGraphics graphics, int scale) {
        super.draw(graphics, "$", "#ff0000", scale);
    }
}

class Position {
    private int x, y;
    public Position(int x, int y) {
        this.x = x; this.y = y;
    }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        Position p = (Position) o;
        return x == p.getX() && y == p.getY();
    }
}
