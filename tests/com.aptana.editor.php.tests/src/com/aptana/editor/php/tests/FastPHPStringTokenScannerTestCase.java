/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.php.tests;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import com.aptana.editor.common.tests.AbstractTokenScannerTestCase;
import com.aptana.editor.php.internal.parser.PHPTokenType;
import com.aptana.editor.php.internal.text.rules.FastPHPStringTokenScanner;

/**
 * @author Max Stepanov
 *
 */
@SuppressWarnings("nls")
public class FastPHPStringTokenScannerTestCase extends AbstractTokenScannerTestCase {

	private IToken defaultToken = getToken("string.quoted.double.php");
	
	/* (non-Javadoc)
	 * @see com.aptana.editor.common.tests.AbstractTokenScannerTestCase#createTokenScanner()
	 */
	@Override
	protected ITokenScanner createTokenScanner() {
		return new FastPHPStringTokenScanner(defaultToken);
	}
	
	private IToken getToken(PHPTokenType type) {
		return new Token(type.toString());
	}

	public void testDefault() {
		String src = " abc ";
		IDocument document = new Document(src);

		scanner.setRange(document, 0, document.getLength());
		assertToken(defaultToken, 0, 5);
	}

	public void testEscapeSpecial() {
		String src = "\\\\ \\\" \\$ \\f \\v \\t \\r \\n";
		IDocument document = new Document(src);

		scanner.setRange(document, 0, document.getLength());
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 0, 2); // \\
		assertToken(defaultToken, 2, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 3, 2); // \"
		assertToken(defaultToken, 5, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 6, 2); // \$
		assertToken(defaultToken, 8, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 9, 2); // \f
		assertToken(defaultToken, 11, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 12, 2); // \v
		assertToken(defaultToken, 14, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 15, 2); // \t
		assertToken(defaultToken, 17, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 18, 2); // \r
		assertToken(defaultToken, 20, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 21, 2); // \n
	}

	public void testEscapeOct() {
		String src = " \\7 \\77 \\777 \\7777";
		IDocument document = new Document(src);

		scanner.setRange(document, 0, document.getLength());
		assertToken(defaultToken, 0, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 1, 2); // \7
		assertToken(defaultToken, 3, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 4, 3); // \77
		assertToken(defaultToken, 7, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 8, 4); // \777
		assertToken(defaultToken, 12, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 13, 4); // \777
		assertToken(defaultToken, 17, 1); // 7
	}

	public void testEscapeHex() {
		String src = " \\xf \\xFF \\xfFF \\xf";
		IDocument document = new Document(src);

		scanner.setRange(document, 0, document.getLength());
		assertToken(defaultToken, 0, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 1, 3); // \xf
		assertToken(defaultToken, 4, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 5, 4); // \xFF
		assertToken(defaultToken, 9, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 10, 4); // \xfFF
		assertToken(defaultToken, 14, 2);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 16, 3); // \xf
	}

	public void testEscapeInvalidChars() {
		String src = " \\8 \\a ";
		IDocument document = new Document(src);

		scanner.setRange(document, 0, document.getLength());
		assertToken(defaultToken, 0, 1);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 1, 1); // \
		assertToken(defaultToken, 2, 2);
		assertToken(getToken(PHPTokenType.CHARACTER_ESCAPE), 4, 1); // \
		assertToken(defaultToken, 5, 2);
	}

	public void testSimpleVariable() {
		String src = " $x. $xyz- $x";
		IDocument document = new Document(src);

		scanner.setRange(document, 0, document.getLength());
		assertToken(defaultToken, 0, 1);
		assertToken(getToken(PHPTokenType.VARIABLE), 1, 2); // $x
		assertToken(defaultToken, 3, 2);
		assertToken(getToken(PHPTokenType.VARIABLE), 5, 4); // $xyz
		assertToken(defaultToken, 9, 2);
		assertToken(getToken(PHPTokenType.VARIABLE), 11, 2); // $x
	}

	public void testSimpleVariableClassOperator() {
		String src = "$x-> $x->xyz $x->y->z";
		IDocument document = new Document(src);

		scanner.setRange(document, 0, document.getLength());
		assertToken(getToken(PHPTokenType.VARIABLE), 0, 2); // $x
		assertToken(getToken(PHPTokenType.CLASS_OPERATOR), 2, 2); // ->
		assertToken(defaultToken, 4, 1);
		assertToken(getToken(PHPTokenType.VARIABLE), 5, 2); // $x
		assertToken(getToken(PHPTokenType.CLASS_OPERATOR), 7, 2); // ->
		assertToken(getToken(PHPTokenType.VARIABLE), 9, 3); // xyz
		assertToken(defaultToken, 12, 1);
		assertToken(getToken(PHPTokenType.VARIABLE), 13, 2); // $x
		assertToken(getToken(PHPTokenType.CLASS_OPERATOR), 15, 2); // ->
		assertToken(getToken(PHPTokenType.VARIABLE), 17, 1); // y
		assertToken(getToken(PHPTokenType.CLASS_OPERATOR), 18, 2); // ->
		assertToken(getToken(PHPTokenType.VARIABLE), 20, 1); // y
	}

	public void testSimpleVariableArrayNumericKey() {
		String src = "$x[0] $x[123]";
		IDocument document = new Document(src);

		scanner.setRange(document, 0, document.getLength());
		assertToken(getToken(PHPTokenType.VARIABLE), 0, 2); // $x
		assertToken(getToken(PHPTokenType.ARRAY_BEGIN), 2, 1); // [
		assertToken(getToken(PHPTokenType.NUMERIC), 3, 1); // 0
		assertToken(getToken(PHPTokenType.ARRAY_END), 4, 1); // ]
		assertToken(defaultToken, 5, 1);
		assertToken(getToken(PHPTokenType.VARIABLE), 6, 2); // $x
		assertToken(getToken(PHPTokenType.ARRAY_BEGIN), 8, 1); // [
		assertToken(getToken(PHPTokenType.NUMERIC), 9, 3); // 123
		assertToken(getToken(PHPTokenType.ARRAY_END), 12, 1); // ]
	}

	public void testSimpleVariableArrayStringKey() {
		String src = "$x[a] $x[abc] ";
		IDocument document = new Document(src);

		scanner.setRange(document, 0, document.getLength());
		assertToken(getToken(PHPTokenType.VARIABLE), 0, 2); // $x
		assertToken(getToken(PHPTokenType.ARRAY_BEGIN), 2, 1); // [
		assertToken(getToken(PHPTokenType.VARIABLE), 3, 1); // a
		assertToken(getToken(PHPTokenType.ARRAY_END), 4, 1); // ]
		assertToken(defaultToken, 5, 1);
		assertToken(getToken(PHPTokenType.VARIABLE), 6, 2); // $x
		assertToken(getToken(PHPTokenType.ARRAY_BEGIN), 8, 1); // [
		assertToken(getToken(PHPTokenType.VARIABLE), 9, 3); // abc
		assertToken(getToken(PHPTokenType.ARRAY_END), 12, 1); // ]
		assertToken(defaultToken, 13, 1);
	}

	public void testSimpleVariableArrayVariableKey() {
		String src = "$x[$a] $x[$abc] ";
		IDocument document = new Document(src);

		scanner.setRange(document, 0, document.getLength());
		assertToken(getToken(PHPTokenType.VARIABLE), 0, 2); // $x
		assertToken(getToken(PHPTokenType.ARRAY_BEGIN), 2, 1); // [
		assertToken(getToken(PHPTokenType.VARIABLE), 3, 2); // $a
		assertToken(getToken(PHPTokenType.ARRAY_END), 5, 1); // ]
		assertToken(defaultToken, 6, 1);
		assertToken(getToken(PHPTokenType.VARIABLE), 7, 2); // $x
		assertToken(getToken(PHPTokenType.ARRAY_BEGIN), 9, 1); // [
		assertToken(getToken(PHPTokenType.VARIABLE), 10, 4); // $abc
		assertToken(getToken(PHPTokenType.ARRAY_END), 14, 1); // ]
	}

	public void testSimpleVariablePunctuation() {
		String src = " ${x}. ${xyz}a";
		IDocument document = new Document(src);

		scanner.setRange(document, 0, document.getLength());
		assertToken(defaultToken, 0, 1);
		assertToken(getToken(PHPTokenType.VARIABLE_PUNCTUATION), 1, 2); // ${
		assertToken(getToken(PHPTokenType.VARIABLE), 3, 1); // x
		assertToken(getToken(PHPTokenType.VARIABLE_PUNCTUATION), 4, 1); // }
		assertToken(defaultToken, 5, 2);
		assertToken(getToken(PHPTokenType.VARIABLE_PUNCTUATION), 7, 2); // ${
		assertToken(getToken(PHPTokenType.VARIABLE), 9, 3); // xyz
		assertToken(getToken(PHPTokenType.VARIABLE_PUNCTUATION), 12, 1); // }
		assertToken(defaultToken, 13, 1);
	}

	public void testComplexVariable() {
		String src = " {$x}. {$xyz}a";
		IDocument document = new Document(src);

		scanner.setRange(document, 0, document.getLength());
		assertToken(defaultToken, 0, 1);
		assertToken(getToken(PHPTokenType.VARIABLE_PUNCTUATION), 1, 1); // {
		assertToken(getToken(PHPTokenType.VARIABLE), 2, 2); // $x
		assertToken(getToken(PHPTokenType.VARIABLE_PUNCTUATION), 4, 1); // }
		assertToken(defaultToken, 5, 2);
		assertToken(getToken(PHPTokenType.VARIABLE_PUNCTUATION), 7, 1); // {
		assertToken(getToken(PHPTokenType.VARIABLE), 8, 4); // $xyz
		assertToken(getToken(PHPTokenType.VARIABLE_PUNCTUATION), 12, 1); // }
		assertToken(defaultToken, 13, 1);
	}

	public void testComplexVariableMultiOperation() {
		String src = "{$x[0]->y[abc][1]->z}";
		IDocument document = new Document(src);

		scanner.setRange(document, 0, document.getLength());
		assertToken(getToken(PHPTokenType.VARIABLE_PUNCTUATION), 0, 1); // {
		assertToken(getToken(PHPTokenType.VARIABLE), 1, 2); // $x
		assertToken(getToken(PHPTokenType.ARRAY_BEGIN), 3, 1); // [
		assertToken(getToken(PHPTokenType.NUMERIC), 4, 1); // 0
		assertToken(getToken(PHPTokenType.ARRAY_END), 5, 1); // ]
		assertToken(getToken(PHPTokenType.CLASS_OPERATOR), 6, 2); // ->
		assertToken(getToken(PHPTokenType.VARIABLE), 8, 1); // y
		assertToken(getToken(PHPTokenType.ARRAY_BEGIN), 9, 1); // [
		assertToken(getToken(PHPTokenType.VARIABLE), 10, 3); // abc
		assertToken(getToken(PHPTokenType.ARRAY_END), 13, 1); // ]
		assertToken(getToken(PHPTokenType.ARRAY_BEGIN), 14, 1); // [
		assertToken(getToken(PHPTokenType.NUMERIC), 15, 1); // 1
		assertToken(getToken(PHPTokenType.ARRAY_END), 16, 1); // ]
		assertToken(getToken(PHPTokenType.CLASS_OPERATOR), 17, 2); // ->
		assertToken(getToken(PHPTokenType.VARIABLE), 19, 1); // y
	}

}