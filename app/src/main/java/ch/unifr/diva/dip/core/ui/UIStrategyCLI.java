package ch.unifr.diva.dip.core.ui;

import java.util.Scanner;

/**
 * UI strategy for the command line interface.
 */
public class UIStrategyCLI implements UIStrategy, Localizable {

	@Override
	public void showError(Throwable throwable) {
		System.out.println(
				localize("error") + ": "
				+ throwable.getMessage()
		);
	}

	@Override
	public void showError(String message, Throwable throwable) {
		System.out.println(localize("error") + ": " + message);
		System.out.println(throwable.getMessage());
	}

	@Override
	public void showInformation(String message) {
		System.out.println(
				localize("information") + ": "
				+ message
		);
	}

	@Override
	public void showWarning(String message) {
		System.out.println(
				localize("warning") + ": "
				+ message
		);
	}

	@Override
	public Answer getAnswer(String message) {
		System.out.println(message);
		Scanner scanner = new Scanner(System.in);
		Answer answer = Answer.NO;
		// TODO: needs to be localized, and generally improved I guess...
		while (answer == null) {
			String line = scanner.nextLine().trim().toLowerCase();
			if (line.startsWith("y")) {
				answer = Answer.YES;
			} else if (line.startsWith("n")) {
				answer = Answer.NO;
			} else if (line.startsWith("c")) {
				answer = Answer.CANCEL;
			}
		}
		System.out.println();
		return answer;
	}

}
