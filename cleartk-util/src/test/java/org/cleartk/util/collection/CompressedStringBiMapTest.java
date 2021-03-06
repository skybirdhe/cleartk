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
package org.cleartk.util.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.cleartk.test.util.DefaultTestBase;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class CompressedStringBiMapTest extends DefaultTestBase {

  @Test
  public void testSerialization() throws FileNotFoundException, IOException {
    CompressedStringBiMap map = new CompressedStringBiMap();
    map.getOrGenerateKey("one");
    map.getOrGenerateKey("two");
    map.getOrGenerateKey("three");
    map.getOrGenerateKey("four");
    map.getOrGenerateKey("five");
    map.getOrGenerateKey("six");

    Writer writer = new FileWriter(new File(outputDirectory, "csbm-test.txt"));
    map.write(writer, true);

    map = new CompressedStringBiMap();
    map.read(new FileReader(new File(outputDirectory, "csbm-test.txt")));

    assertEquals("0", map.inverse().get("one"));
    assertEquals("1", map.inverse().get("two"));
    assertEquals("2", map.inverse().get("three"));
    assertEquals("3", map.inverse().get("four"));
    assertEquals("4", map.inverse().get("five"));
    assertEquals("5", map.inverse().get("six"));

    assertNull(map.inverse().get("seven"));
    assertEquals("6", map.getOrGenerateKey("seven"));

    map.inverse().remove("one");
    assertNull(map.inverse().get("one"));
    assertEquals("7", map.getOrGenerateKey("one"));

    writer = new FileWriter(new File(outputDirectory, "csbm-test.txt"));
    map.write(writer);
    map = new CompressedStringBiMap();
    map.read(new FileReader(new File(outputDirectory, "csbm-test.txt")));
    assertEquals("7", map.getOrGenerateKey("one"));
    assertEquals("8", map.getOrGenerateKey("eight"));

  }
}
