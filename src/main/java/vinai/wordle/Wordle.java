/**
 * 
 */
package vinai.wordle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author vinai-11009
 *
 */
public class Wordle extends JFrame
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2571486644564023573L;
	
	private final static String FILELOCATION = "resources/english.txt";
	private HashSet<String> wordSet;
	private Integer noOfLetters;
	private String unusedLetters;
	private String[] misplacedLetters;
	private String[] confirmedLetters;
	private JPanel mainPanel;
	private JLabel noOfLettersLabel;
	private JSlider noOfLettersSlider;
	private JLabel unusedLettersLabel;
	private JTextArea unusedLettersArea;
	private JLabel wrongPlacedLettersLabel;
	private JPanel wrongPlacedLettersPanel;
	private JLabel rightPlacedLettersLabel;
	private JPanel rightlacedLettersPanel;
	private JLabel suggestionLabel;
	private JTextArea suggestionArea;

	/**
	 * @throws HeadlessException
	 */
	public Wordle() throws HeadlessException
	{
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setNoOfLetters(5);
		this.setTitle("Wordle");
		this.setLayout(new BorderLayout());
		this.mainPanel = new JPanel(new GridBagLayout());
		this.setMainPanel();		
		this.add(mainPanel);
	}
	
	private void setMainPanel()	{				
		GridBagConstraints gbcLeft = new GridBagConstraints();
		gbcLeft.insets = new Insets(10, 10, 10, 3);		
        gbcLeft.weightx = 1;
		gbcLeft.weighty = 0;
		gbcLeft.gridx = 0;
		gbcLeft.gridy = 0;
		gbcLeft.fill = GridBagConstraints.HORIZONTAL;
		
		GridBagConstraints gbcRight = new GridBagConstraints();
		gbcRight.insets = new Insets(10, 3, 10, 10);		
        gbcRight.weightx = 1;
		gbcRight.weighty = 0;
		gbcRight.gridx = 1;
		gbcRight.gridy = 0;
		gbcRight.fill = GridBagConstraints.HORIZONTAL;
		
		this.noOfLettersLabel = new JLabel("Number of Letters : ");
		this.mainPanel.add(this.noOfLettersLabel,gbcLeft);
		
		this.noOfLettersSlider = new JSlider(4, 11, this.getNoOfLetters());
		this.noOfLettersSlider.setMajorTickSpacing(1);  
		this.noOfLettersSlider.setPaintTicks(true);  
		this.noOfLettersSlider.setPaintLabels(true);  
		this.noOfLettersSlider.addChangeListener(this.sliderChangeListener);
		mainPanel.add(this.noOfLettersSlider,gbcRight);
		
		gbcLeft.gridy++;
		gbcRight.gridy++;
		
		this.rightPlacedLettersLabel = new JLabel("Right Placed Letters : ");
		this.mainPanel.add(this.rightPlacedLettersLabel,gbcLeft);		
	
		this.rightlacedLettersPanel = new JPanel(new GridBagLayout());
		this.mainPanel.add(this.rightlacedLettersPanel,gbcRight);
		
		gbcLeft.gridy++;
		gbcRight.gridy++;
		
		this.wrongPlacedLettersLabel = new JLabel("Wrong Placed Letters : ");
		this.mainPanel.add(this.wrongPlacedLettersLabel,gbcLeft);
	
		this.wrongPlacedLettersPanel = new JPanel(new GridBagLayout());
		this.mainPanel.add(this.wrongPlacedLettersPanel,gbcRight);		
		
		gbcLeft.gridy++;
		gbcRight.gridy++;
		
		this.unusedLettersLabel = new JLabel("Unused Letters : ");
		this.mainPanel.add(this.unusedLettersLabel,gbcLeft);

		this.unusedLettersArea = new JTextArea(3,33);
		this.unusedLettersArea.getDocument().putProperty("owner", this.unusedLettersArea);
		this.unusedLettersArea.getDocument().addDocumentListener(this.textChangeListener);
		this.mainPanel.add(this.unusedLettersArea,gbcRight);		
		
		gbcLeft.gridy++;
		gbcRight.gridy++;
		
		this.suggestionLabel = new JLabel("Suggested Words : ");
		this.mainPanel.add(this.suggestionLabel,gbcLeft);

		this.suggestionArea = new JTextArea(13,8);
		this.suggestionArea.setEditable(Boolean.FALSE);
		this.mainPanel.add(new JScrollPane(this.suggestionArea),gbcRight);
		
		resetGame();
	}
	
	private void setLetterPanel (JPanel panel, String namePrefix) {
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(1,1, 0, 0);		
        gbc.weightx = 15;
		gbc.weighty = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		for(Integer i=0; i<getNoOfLetters(); i++){
			gbc.gridx = i;
			JTextField txtBox = new JTextField(2);
			txtBox.setName(namePrefix + i.toString());
			txtBox.getDocument().putProperty("owner", txtBox);
			txtBox.getDocument().addDocumentListener(this.textChangeListener);
			panel.add(txtBox,gbc);
		}
		panel.revalidate();
	}
	
	public Integer getNoOfLetters(){
		return noOfLetters;
	}
	
	public void setNoOfLetters(Integer noOfLetters){
		if(noOfLetters > 3 && noOfLetters < 12){
			this.noOfLetters = noOfLetters;
		}
	}
	
	public String getUnusedLetters(){
		if(unusedLetters == null) return "";
		return unusedLetters;
	}
	
	public void setUnusedLetters (String letters) {
		StringBuilder sbLetters = new StringBuilder();
		if(letters != null){
			//modLetters = letters.toLowerCase().replaceAll("[^a-z]+","");
			char[] chars = letters.toCharArray();
			Set<Character> charSet = new LinkedHashSet<Character>();
			for (char c : chars) {
				charSet.add(Character.toUpperCase(c));
			}

			
			for (Character character : charSet) {
				if(character >= 'A' && character <= 'Z' ){					
					sbLetters.append(character);
				}
			}
		}
		unusedLetters = sbLetters.toString();
		
		final String modLettersfinal = unusedLetters;
		
		if(!unusedLetters.equals(letters)) {
			Runnable updateUI = new Runnable() {
				@Override
				public void run() {
					unusedLettersArea.setText(modLettersfinal);
				}
			};			
			SwingUtilities.invokeLater(updateUI);	
		}			
	}
	
	public String[] getMisplacedLetters(){
		return misplacedLetters;
	}
	
	public void setMisplacedLetters (Integer position, String letters) {
		if(position >= getNoOfLetters()) return;
		StringBuilder sbLetters = new StringBuilder();
		if(letters != null){
			char[] chars = letters.toCharArray();
			for (char c : chars) {
				Character character = Character.toUpperCase(c);
				if(character >= 'A' && character <= 'Z' ){					
					sbLetters.append(character);
				}				
				if(sbLetters.length() >= getNoOfLetters() -1 ) break;
			}
		}
		misplacedLetters[position] = sbLetters.toString();
		
		final String modLettersfinal = misplacedLetters[position];
		final Integer positionfinal = position;
		
		if(!misplacedLetters[position].equals(letters)) {
			Runnable updateUI = new Runnable() {
				@Override
				public void run() {
					((JTextField)wrongPlacedLettersPanel.getComponent(positionfinal)).setText(modLettersfinal);
				}
			};			
			SwingUtilities.invokeLater(updateUI);	
		}			
	}
	
	public void setConfirmLetters (Integer position, String letters) {
		if(position >= getNoOfLetters()) return;
		String letter = "";
		if(letters != null){
			char[] chars = letters.toCharArray();
			for (int itr=chars.length-1;itr>=0;itr--) {
				Character character = Character.toUpperCase(chars[itr]);
				if(character >= 'A' && character <= 'Z' ){					
					letter=character.toString();
					break;
				}
			}
		}
		confirmedLetters[position] = letter;
		
		final String modLettersfinal = confirmedLetters[position];
		final Integer positionfinal = position;
		
		if(!confirmedLetters[position].equals(letters)) {
			Runnable updateUI = new Runnable() {
				@Override
				public void run() {
					((JTextField)rightlacedLettersPanel.getComponent(positionfinal)).setText(modLettersfinal);
				}
			};			
			SwingUtilities.invokeLater(updateUI);	
		}else if(!confirmedLetters[position].equals("")){
			Runnable updateUI = new Runnable() {
				@Override
				public void run() {
					Component[] components = wrongPlacedLettersPanel.getComponents();
					for (Component component : components) {
						JTextField jTextField = (JTextField)component;
						jTextField.setText(jTextField.getText().replaceFirst(modLettersfinal,""));
					}
				}
			};			
			SwingUtilities.invokeLater(updateUI);
		}			
	}
	
	public synchronized void resetGame(){
		Integer noOfLetters = getNoOfLetters();
		misplacedLetters = new String[noOfLetters];
		confirmedLetters = new String[noOfLetters];
		
		this.unusedLettersArea.setText("");
		
		this.rightlacedLettersPanel.removeAll();
		this.setLetterPanel(this.rightlacedLettersPanel, "r");
		
		this.wrongPlacedLettersPanel.removeAll();
		this.setLetterPanel(this.wrongPlacedLettersPanel, "w");				
		
		resetWordSet();		
		printWordSet();
		
	}
	
	public void resetWordSet(){		
		Pattern pattern = Pattern.compile("^[A-Z]+$");
		this.wordSet = new HashSet<String>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(FILELOCATION));
			String line = reader.readLine();
			while (line != null) {
				line = line.trim().toUpperCase();
				if(line.length() == getNoOfLetters()){
					if(pattern.matcher(line).find()){
						wordSet.add(line);
					}
				}				 
				
				// read next line
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			System.out.println("Working Directory = " + System.getProperty("user.dir"));
			e.printStackTrace();
			
		}
	}
	
	public void printWordSet(){	
		StringBuffer output = new StringBuffer("");
		ArrayList<String> remainingList = getOutputList();
		Iterator<String> itr=remainingList.iterator();  
		while(itr.hasNext())  
		{
			output.append(itr.next());
			output.append(System.getProperty("line.separator"));
		}	
		suggestionArea.setText(output.toString());
	}
	
	public ArrayList<String> getOutputList() {
		ArrayList<String> remaingWords = new ArrayList<>(wordSet);
		
		String uul = getUnusedLetters();
		
		Iterator<String> itr=remaingWords.iterator(); 
		while(itr.hasNext()) {			
			Boolean remove = false;
			StringBuilder word = new StringBuilder(itr.next());
			
			for(int i = 0; i < getNoOfLetters() ; i++ ) {
				String confrimedLetter = confirmedLetters[i];
				if(confrimedLetter == null || confrimedLetter.length() == 0) continue;
				if(confrimedLetter.charAt(0) != word.charAt(i)){
					remove = true;
					break;
				}
				word.setCharAt(i,' ');
			}
			
			if(remove) {
				itr.remove();
				continue;
			}
			
			String word2 = word.toString();
			for(int i = 0; i < getNoOfLetters() ; i++ ) {	
				String misplacedLetter = misplacedLetters[i];
				if(misplacedLetter == null || misplacedLetter.length() == 0) continue;
				
				StringBuilder word3 = new StringBuilder(word2);
				for(int j=0; j<misplacedLetter.length(); j++){
					char letter = misplacedLetter.charAt(j);
					Integer index = word3.indexOf(String.valueOf(letter));
					if(index == -1){
						remove = true;
						break;
					}
					if(letter == word3.charAt(i)){
						remove = true;
						break;
					}
					word3.setCharAt(index,' ');
					word.setCharAt(index,' ');
				}
				if(remove) break;
			}
			
			if(remove) {
				itr.remove();
				continue;
			}
			
			for (int i = 0; i < uul.length(); i++){
				char letter = uul.charAt(i); 
				if(word.indexOf(String.valueOf(letter)) != -1){
					remove = true;
					break;
				}				
			}
			
			if(remove) {
				itr.remove();
				continue;
			}
			
		}
		
		
		
		
		Collections.sort(remaingWords.subList(0, remaingWords.size()));
		return remaingWords;
	}
	
	ChangeListener sliderChangeListener = new ChangeListener() 
	{
		public void stateChanged(ChangeEvent ce) {
			JSlider jSlider = (JSlider)ce.getSource();
			if (!jSlider.getValueIsAdjusting()) {	
				Integer value = jSlider.getValue();
				setNoOfLetters(value);
				resetGame();
			}
        }
    };
	
	DocumentListener textChangeListener = new DocumentListener() 
	{
		public void changedUpdate(DocumentEvent de) {
			changed(de);
		}
		public void removeUpdate(DocumentEvent de) {
			changed(de);
		}
		public void insertUpdate(DocumentEvent de) {
			changed(de);
		}

		public void changed(DocumentEvent de) {
			Object source = de.getDocument().getProperty("owner");
			if (source == unusedLettersArea) {
				setUnusedLetters(unusedLettersArea.getText());
			} else if(source.getClass().getSimpleName().equals("JTextField")) {
				JTextField jTextField = (JTextField)source;
				String srcName = jTextField.getName();
				String value = jTextField.getText();
				Integer postion = Integer.parseInt(srcName.substring(1));
				if(srcName.charAt(0) == 'w') {
					setMisplacedLetters(postion,value);
				}
				if(srcName.charAt(0) == 'r') {
					setConfirmLetters(postion,value);
				}
			} 
			printWordSet();
		}
	};

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Wordle frame = new Wordle();	
		frame.pack();
        frame.setLocationRelativeTo(null);	
		frame.setResizable(false);
		frame.setVisible(true);

	}

}
