package com.imcode.imcms.api;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Johan Larsson imCode +46(0)498 200 300
 * Date: 2005-dec-15
 * Time: 10:07:59
 * Title: com.imcode.imcms.apiSpellChecker
 * Description:
 */
public class ImCMSSpellChecker implements SpellCheckListener {
	private static String path = "D:/Projekt/imCMS-spell/";
	private static String dictFile = path + "/swedish.0";
	private static String phonetFile = path + "/phonet.sv";
	private ArrayList missSpelledWords = new ArrayList( );
	private SpellChecker spellCheck = null;

	public ImCMSSpellChecker(String wordToCheck) throws IOException {
		SpellDictionary dictionary = new SpellDictionaryHashMap( new File( dictFile ), new File( phonetFile ) );

		spellCheck = new SpellChecker( dictionary );
		spellCheck.addSpellCheckListener( this );

		spellCheck.checkSpelling( new StringWordTokenizer( wordToCheck ) );

	}

	public void spellingError( SpellCheckEvent event ) {
		List suggestions = event.getSuggestions();
		MissSpelledWord newWord = new MissSpelledWord( event.getInvalidWord() );
		if( suggestions.size() > 0 ) {
			for ( Iterator suggestedWord = suggestions.iterator(); suggestedWord.hasNext(); ) {
				newWord.addSuggestions( suggestedWord.next().toString() );
			}
		}
		missSpelledWords.add( newWord );
	}

	public ArrayList getMissSpelledWords() {
		return missSpelledWords;
	}

	public class MissSpelledWord {
		private String word;
		private ArrayList suggestions;

		public MissSpelledWord( String word ) {
			this.word = word;
			this.suggestions = new ArrayList( );
		}

		public String getWord() {
			return word;
		}

		public ArrayList getSuggestions() {
			return suggestions;
		}

		public void addSuggestions( String suggestion ) {
			this.suggestions.add( suggestion );
		}
	}
}
