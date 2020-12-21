import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

class ConsolePanel implements Runnable {

	static final int NUMLINES = 16;

	JFrame frame;
	JPanel panel;
	JTextArea[] textAreas;
	PrintStream[] printStreams;
	int[] textLines;
	String[] columnLabels;

	ArrayBlockingQueue<Pair<Integer, String>> messages = new ArrayBlockingQueue<>(32);

	public ConsolePanel(int numPanels) {
		frame = new JFrame();
		textAreas = new JTextArea[numPanels];
		printStreams = new PrintStream[numPanels];
		textLines = new int[numPanels];
		init();
	}

	public ConsolePanel(int numPanels, String winTitle) {
		frame = new JFrame(winTitle);
		textAreas = new JTextArea[numPanels];
		printStreams = new PrintStream[numPanels];
		textLines = new int[numPanels];
		init();
	}

	public ConsolePanel(int numPanels, String winTitle, String[] columnLabels) {
		frame = new JFrame(winTitle);
		textAreas = new JTextArea[numPanels];
		printStreams = new PrintStream[numPanels];
		textLines = new int[numPanels];
		if (columnLabels.length >= numPanels) {
			this.columnLabels = columnLabels;
		}
		initialize();
	}

	private void initialize() {
		frame.setLocation(0, 0);
		frame.setSize(10, 10);
		frame.setResizable(true);

		frame.setLayout(new BorderLayout());

		panel = new JPanel();
		frame.add(panel, BorderLayout.CENTER);

		for (int i = 0; i < textAreas.length; i++) {
			textAreas[i] = new JTextArea(NUMLINES, columnLabels[i].length());
			textAreas[i].setBackground(Color.BLACK);
			textAreas[i].setForeground(Color.LIGHT_GRAY);
			textAreas[i].setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));
			textAreas[i].setLineWrap(true);
			textAreas[i].setEditable(false);
			printStreams[i] = getOutputStream(i);
			panel.add(textAreas[i]);
		}

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public void close() {
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}
	
	public ArrayBlockingQueue<Pair<Integer, String>> getPrintQueue() {
		return messages;
	}

	public PrintStream getOutputStream(int i) {
		if (i < printStreams.length && printStreams[i] != null) {
			return printStreams[i];
		}
		return new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				textAreas[i].append(String.valueOf((char) b));
			}
		});
	}

	public PrintStream[] getOutputStream(int[] arrI) {
		PrintStream[] pS = new PrintStream[arrI.length];
		for (int i = 0; i < arrI.length; i++) {
			if (arrI[i] >= 0 && arrI[i] < printStreams.length) {
				pS[i] = printStreams[arrI[i]];
			}
		}
		return pS;
	}

	public JTextArea getTextArea(int i) {
		if (i < textAreas.length) {
			return textAreas[i];
		}
		return null;
	}

	public JTextArea[] getTextArea(int[] arrI) {
		JTextArea[] jTA = new JTextArea[arrI.length];
		for (int i = 0; i < arrI.length; i++) {
			if (arrI[i] >= 0 && arrI[i] < textAreas.length) {
				jTA[i] = textAreas[arrI[i]];
			}
		}
		return jTA;
	}

	public void setOut(int i) {
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				textAreas[i].append(String.valueOf((char) b));
			}
		}));
//		for (JTextArea text : textAreas) {
//			panel.add(text);
//		}
	}

	public int countMatches(String s, char c) {
		int count = 0;
		for (char a : s.toCharArray()) {
			if (a == c) {
				count++;
			}
		}
		return count;
	}

	@Override
	public void run() {
		Pair<Integer, String> m;
		if (columnLabels != null && columnLabels.length >= textAreas.length) {
			for (int i = 0; i < textAreas.length; i++) {
				setOut(i);
				System.out.println(columnLabels[i]);
				textLines[i] += 2;
			}
		}
		for (;;) {
			do {
				m = messages.poll();
			} while (m == null);
			setOut(m.getKey());
			if (textLines[m.getKey()] + 1 >= NUMLINES) {
				textAreas[m.getKey()].setText("");
				if (columnLabels != null) {
					System.out.println(columnLabels[m.getKey()]);
					textLines[m.getKey()] = 2 + countMatches(columnLabels[m.getKey()], '\n');

				} else {
					textLines[m.getKey()] = 0;
				}
			}
			System.out.println(m.getValue());
			textLines[m.getKey()]++;
			System.out.flush();
		}

	}

}
