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
package org.cleartk.corpus.conll2005;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.srl.type.Argument;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class Conll2005Writer extends JCasAnnotator_ImplBase {

  @ConfigurationParameter(
      name = PARAM_OUTPUT_FILE,
      mandatory = true,
      description = "the path where the CoNLL-2005-formatted text should be written")
  private File outputFile;

  public static final String PARAM_OUTPUT_FILE = "outputFile";

  private PrintWriter output;

  private boolean first;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    try {
      this.output = new PrintWriter(outputFile);
      this.first = true;
    } catch (FileNotFoundException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
    for (Sentence sentence : sentences) {
      if (first)
        first = false;
      else
        output.println();

      List<PredicateWriter> predicateWriters = new ArrayList<PredicateWriter>();
      for (Predicate predicate : JCasUtil.selectCovered(jCas, Predicate.class, sentence)) {
        predicateWriters.add(new PredicateWriter(jCas, predicate));
      }

      for (Token token : JCasUtil.selectCovered(jCas, Token.class, sentence)) {
        Conll05Line line = new Conll05Line();

        // line.setLexeme(token.getCoveredText());
        // line.setPos(token.getPos());

        for (PredicateWriter predicateWriter : predicateWriters) {
          predicateWriter.write(token, line);
        }

        output.println(line.evalString());
      }

    }
    output.flush();
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    output.close();
    super.collectionProcessComplete();
  }

  private static class Conll05Line {
    // String lexeme;
    // String pos;
    // String syntaxSegment;
    // String neSegment;
    // String predicateFrameset;
    String predicateBaseform;

    List<String> argumentRoles;

    public Conll05Line() {
      // lexeme = "<empty>";
      // pos = "<empty>";
      // syntaxSegment = "*";
      // neSegment = "*";
      // predicateFrameset = "-";
      predicateBaseform = "-";
      argumentRoles = new ArrayList<String>();
    }

    public String evalString() {
      StringBuffer buffer = new StringBuffer();
      // buffer.append(lexeme);
      // buffer.append("\t");
      // buffer.append(pos);
      // buffer.append("\t");
      // buffer.append(syntaxSegment);
      // buffer.append("\t");
      // buffer.append(neSegment);
      // buffer.append("\t");
      // buffer.append("predicateFrameset");
      // buffer.append("\t");
      buffer.append(predicateBaseform);

      for (String argumentRole : argumentRoles) {
        buffer.append("\t");
        buffer.append(argumentRole);
      }

      return buffer.toString();
    }

    // public void setLexeme(String lexeme) {
    // this.lexeme = lexeme;
    // }
    //
    // public void setPos(String pos) {
    // this.pos = pos;
    // }

    // public void setSyntaxSegment(String syntaxSegment) {
    // this.syntaxSegment = syntaxSegment;
    // }

    // public void setNeSegment(String neSegment) {
    // this.neSegment = neSegment;
    // }

    public void setPredicateFrameset(Integer predicateFrameset) {
      // if( predicateFrameset == null ) {
      // this.predicateFrameset = "-";
      // } else {
      // this.predicateFrameset = String.format("%2d", predicateFrameset);
      // }
    }

    public void setPredicateBaseform(String predicateBaseform) {
      if (predicateBaseform == null) {
        this.predicateBaseform = "-";
      } else {
        this.predicateBaseform = predicateBaseform;
      }
    }

    public void addArgumentRole(String argumentRole) {
      this.argumentRoles.add(argumentRole);
    }
  }

  private static class PredicateWriter {
    String baseform;

    Integer frameset;

    Token token;

    List<ArgumentWriter> argumentWriters;

    PredicateWriter(JCas jCas, Predicate predicate) {
      this.token = JCasUtil.selectCovered(jCas, Token.class, predicate.getAnnotation()).get(0);
      this.baseform = predicate.getBaseForm();
      this.frameset = 1;

      Collection<Argument> allArgs = JCasUtil.select(predicate.getArguments(), Argument.class);
      this.argumentWriters = new ArrayList<ArgumentWriter>();
      for (Argument arg : allArgs) {
        if (arg instanceof SemanticArgument) {
          this.argumentWriters.add(new ArgumentWriter(jCas, (SemanticArgument) arg));
        }
      }
    }

    void write(Token tok, Conll05Line line) {
      if (this.token.equals(tok)) {
        line.setPredicateBaseform(this.baseform);
        line.setPredicateFrameset(this.frameset);
      }

      line.addArgumentRole(getArgumentsString(tok));
    }

    String getArgumentsString(Token tok) {
      StringBuffer buffer = new StringBuffer();

      for (ArgumentWriter argumentWriter : this.argumentWriters) {
        buffer.append(argumentWriter.getStartString(tok));
      }

      buffer.append("*");

      for (ArgumentWriter argumentWriter : this.argumentWriters) {
        buffer.append(argumentWriter.getEndString(tok));
      }

      return buffer.toString();
    }
  }

  private static class ArgumentWriter {
    String label;

    String feature;

    String preposition;

    List<Token> tokens;

    ArgumentWriter(JCas jCas, SemanticArgument arg) {
      this.label = arg.getLabel();
      this.feature = arg.getFeature();
      this.preposition = arg.getPreposition();
      this.tokens = JCasUtil.selectCovered(jCas, Token.class, arg.getAnnotation());
    }

    String getStartString(Token token) {
      if (token == this.tokens.get(0))
        return "(" + getFullLabel();
      else
        return "";
    }

    String getEndString(Token token) {
      if (token == this.tokens.get(this.tokens.size() - 1))
        return ")";
      else
        return "";
    }

    String getFullLabel() {
      StringBuffer buffer = new StringBuffer();

      buffer.append(this.label);
      if (this.feature != null)
        buffer.append("-" + this.feature);
      if (this.preposition != null)
        buffer.append("-" + this.preposition);

      return buffer.toString();
    }
  }

}
