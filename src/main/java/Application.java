import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.input.KeyStroke;

import javax.swing.*;
import java.io.IOException;

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
    private Hero hero = new Hero(10, 10);

    public Game () throws IOException {
        TerminalSize terminalSize = new TerminalSize(40,20);
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
    }

    private void draw() throws IOException {
        screen.clear();
        hero.draw(screen);
        screen.refresh();
    }

    private boolean processKey(KeyStroke key) throws IOException {
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
                if (key.getCharacter() == 'q'){
                    screen.close();
                    return false;
                }
                break;
            case EOF:
                return false;
        }
        return true;
    }

    private void moveHero(Position pos){ hero.setPos(pos); }
}

class Hero {
    private Position pos;
    public Hero() { pos = new Position(10, 10); }
    public Hero(int x, int y) { pos = new Position(x, y); }

    public Position getPos(){ return pos; }
    public void setPos(Position pos){ this.pos = pos; }

    public Position moveUp() { return new Position(pos.getX(), pos.getY() - 1); }
    public Position moveDown() { return new Position(pos.getX(), pos.getY() + 1); }
    public Position moveLeft() { return new Position(pos.getX() - 1, pos.getY()); }
    public Position moveRight() { return new Position(pos.getX() + 1, pos.getY()); }

    public void draw(Screen screen){
        screen.setCharacter(pos.getX(), pos.getY(), TextCharacter.fromCharacter('X')[0]);
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
}
