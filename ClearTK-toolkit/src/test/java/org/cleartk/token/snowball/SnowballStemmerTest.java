 /** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
*/
package org.cleartk.token.snowball;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.CleartkComponents;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.testing.factory.TokenFactory;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 */
public class SnowballStemmerTest {

	@Test
	public void testBadStemmerName() throws UIMAException {
		try {
			AnalysisEngineFactory.createPrimitive(
					SnowballStemmer.class,
					TypeSystemDescriptionFactory.createTypeSystemDescription(Token.class, Sentence.class),
					SnowballStemmer.PARAM_STEMMER_NAME, "FooBar");
			Assert.fail("Expected exception for bad stemmer name");
		} catch (ResourceInitializationException e) {}
	}
	
	@Test
	public void testSimple() throws UIMAException {
		TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory
				.createTypeSystemDescription(Token.class, Sentence.class);
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				DefaultSnowballStemmer.class, typeSystemDescription,
				DefaultSnowballStemmer.PARAM_STEMMER_NAME, "English");
		JCas jCas = engine.newJCas();
		String text = "The brown foxes jumped quickly over the lazy dog.";
		String tokens = "The brown foxes jumped quickly over the lazy dog .";
		TokenFactory.createTokens(jCas, text, Token.class, Sentence.class, tokens);
		engine.process(jCas);
		List<String> actual = new ArrayList<String>();
		for (Token token: AnnotationRetrieval.getAnnotations(jCas, Token.class)) {
			actual.add(token.getStem());
		}
		String expected = "the brown fox jump quick over the lazi dog .";
		Assert.assertEquals(Arrays.asList(expected.split(" ")), actual);
	}

	@Test
	public void testUppercase() throws UIMAException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				DefaultSnowballStemmer.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription(Token.class, Sentence.class),
				SnowballStemmer.PARAM_STEMMER_NAME, "English");
		JCas jCas = engine.newJCas();
		TokenFactory.createTokens(jCas,
				"The brown foxes JumPEd QUICKLy over the lazY dog.",
				Token.class, Sentence.class, 
				"The brown foxes JumPEd QUICKLy over the lazY dog .");
		engine.process(jCas);
		
		List<String> expected = Arrays.asList(
				"the brown fox jump quick over the lazi dog .".split(" "));
		List<String> actual = new ArrayList<String>();
		for (Token token: AnnotationRetrieval.getAnnotations(jCas, Token.class)) {
			actual.add(token.getStem());
		}
		Assert.assertEquals(expected, actual);
		
	}

	@Test
	public void testDescriptor() throws UIMAException, IOException {
		ResourceInitializationException rie = null;
		try {
			CleartkComponents.createPrimitive(DefaultSnowballStemmer.class);
		} catch(ResourceInitializationException e) {
			rie = e;
		}
		
		assertNotNull(rie);
		
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine("org.cleartk.token.snowball.DefaultSnowballStemmer",
				SnowballStemmer.PARAM_STEMMER_NAME, "English");

		Object stemmerName = engine.getConfigParameterValue(SnowballStemmer.PARAM_STEMMER_NAME);
		Assert.assertEquals("English", stemmerName);
		
		engine.collectionProcessComplete();
	}
	
	
	
	
}





