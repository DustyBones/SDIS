package maze.cli;

import maze.logic.Game;

import java.io.Console;
import java.io.IOException;
import java.util.Scanner;

public class test {

	public static void main(String args[]) {
		int option = 0;
		boolean done = false;

		printMenu();
		option = scan.nextInt();

		switch (option) {
		case 1:
			break;
		case 2:
			System.out.print("\n    Quitting game! \n");
			break;
		default:
			System.out.print("\n\n 	Invalid input! Try again!\n");
			break;

		}

		if (game.getHero().isAlive() == true) {
			System.out.print("\n"
					+ "-------- Congratulations! You Win! ----------\n\n");
		} else {
			System.out.print("\n"
					+ "-------- Try Again! You Lose! ----------\n\n");
		}

		scan.close();
	}

}
