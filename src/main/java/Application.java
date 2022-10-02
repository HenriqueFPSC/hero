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

public class Application {
    public static void main(String[] args) {
        try {
            Game game = new Game();
            game.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Game {
    private Screen screen;
    private Arena arena;

    public Game () throws IOException {
        int width = 40, height = 20;
        TerminalSize terminalSize = new TerminalSize(width,height);
        arena = new Arena(width, height);
        DefaultTerminalFactory terminalFactory = new
                DefaultTerminalFactory().setInitialTerminalSize(terminalSize);
        Terminal terminal = terminalFactory.createTerminal();
        screen = new TerminalScreen(terminal);
        screen.setCursorPosition(null); // we don't need a cursor
        screen.startScreen(); // screens must be started
        screen.doResizeIfNecessary(); // resize screen if necessary
    }

    public void run() throws IOException {
        boolean isRunning = true;
        while(isRunning) {
            draw();
            KeyStroke key = screen.readInput();
            isRunning = processKey(key);
        }
        screen.close();
    }

    private void draw() throws IOException {
        screen.clear();
        arena.draw(screen.newTextGraphics());
        screen.refresh();
    }

    private boolean processKey(KeyStroke key){
       return arena.processKey(key);
    }
}

class Hero {
    private Position pos;
    public Hero(int x, int y) { pos = new Position(x, y); }

    public Position getPos(){ return pos; }
    public void setPos(Position pos){ this.pos = pos; }

    public Position moveUp() { return new Position(pos.getX(), pos.getY() - 1); }
    public Position moveDown() { return new Position(pos.getX(), pos.getY() + 1); }
    public Position moveLeft() { return new Position(pos.getX() - 1, pos.getY()); }
    public Position moveRight() { return new Position(pos.getX() + 1, pos.getY()); }

    public void draw(TextGraphics graphics){
        graphics.setForegroundColor(TextColor.Factory.fromString("#FFFFFF"));
        graphics.enableModifiers(SGR.BOLD);
        graphics.putString(new TerminalPosition(pos.getX(), pos.getY()), "X");
    }
}

class Position {
    private int x, y;
    public Position(int x, int y){
        this.x = x; this.y = y;
    }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        Position p = (Position) o;
        return x == p.getX() && y == p.getY();
    }
}

class Arena {
    private int width, height;
    private List<Wall> walls;
    private Hero hero = new Hero(10, 10);
    public Arena(int width, int height){
        this.width = width;
        this.height = height;
        walls = createWalls();
    }

    public boolean processKey(KeyStroke key){
        switch (key.getKeyType()) {
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
    private void moveHero(Position pos){
        if (canHeroMove(pos))
            hero.setPos(pos);
    }
    private boolean canHeroMove(Position pos){
        if (pos.getX() < 0) return false;
        if (pos.getX() >= width) return false;
        if (pos.getY() < 0) return false;
        if (pos.getY() >= height) return false;
        for (Wall wall : walls)
            if (pos.equals(wall.getPos()))
                return false;
        return true;
    }
    public void draw(TextGraphics graphics){
        graphics.setBackgroundColor(TextColor.Factory.fromString("#336699"));
        graphics.fillRectangle(new TerminalPosition(0,0), new TerminalSize(width, height), ' ');
        hero.draw(graphics);
        for (Wall wall : walls) wall.draw(graphics);
    }

    private List<Wall> createWalls() {
        List<Wall> walls = new ArrayList<>();
        for (int c = 0; c < width; c++) {
            walls.add(new Wall(c, 0));
            walls.add(new Wall(c, height - 1));
        }
        for (int r = 1; r < height - 1; r++) {
            walls.add(new Wall(0, r));
            walls.add(new Wall(width - 1, r));
        }
        return walls;
    }
}

class Wall {
    private Position pos;
    public Wall(int x, int y) { pos = new Position(x, y); }

    public Position getPos(){ return pos; }
    public void setPos(Position pos){ this.pos = pos; }

    public void draw(TextGraphics graphics){
        graphics.setForegroundColor(TextColor.Factory.fromString("#FFFFFF"));
        graphics.enableModifiers(SGR.BOLD);
        graphics.putString(new TerminalPosition(pos.getX(), pos.getY()), "O");
    }
}
