package org.apache.jasper.runtime;

import java.io.IOException;

import com.google.autoesc.MemoizingHTMLEscapingWriter;
import javax.servlet.jsp.JspWriter;

/**
 * A JSPWriter wrapper that contextually autoescapes HTML content.
 */
public final class AutoescapingJspWriter extends JspWriter {

  private final JspWriter underlying;
  private final MemoizingHTMLEscapingWriter escapingWriter;

  /**
   * @param underlying receives autoescaped output.
   */
  public AutoescapingJspWriter(JspWriter underlying) {
    super(underlying.getBufferSize(), underlying.isAutoFlush());
    this.underlying = underlying;
    this.escapingWriter = new MemoizingHTMLEscapingWriter(underlying);
  }

  @Override
  public void newLine() throws IOException {
    this.writeKnownSafeContent("\n");
  }

  @Override
  public void print(boolean b) throws IOException {
    escapingWriter.write(Boolean.valueOf(b));
  }

  @Override
  public void print(char c) throws IOException {
    escapingWriter.write(Character.valueOf(c));
  }

  @Override
  public void print(int i) throws IOException {
    // Do not delegate to write(int) which takes a codepoint
    escapingWriter.write(Integer.valueOf(i));
  }

  @Override
  public void print(long l) throws IOException {
    escapingWriter.write(Long.valueOf(l));
  }

  @Override
  public void print(float f) throws IOException {
    escapingWriter.write(Float.valueOf(f));
  }

  @Override
  public void print(double d) throws IOException {
    escapingWriter.write(Double.valueOf(d));
  }

  @Override
  public void print(char[] s) throws IOException {
    escapingWriter.write(s);
  }

  @Override
  public void print(String s) throws IOException {
    escapingWriter.write(s);
  }

  @Override
  public void print(Object obj) throws IOException {
    escapingWriter.write(obj);
  }

  @Override
  public void println() throws IOException {
    this.newLine();
  }

  @Override
  public void println(boolean x) throws IOException {
    this.print(x);
    this.newLine();
  }

  @Override
  public void println(char x) throws IOException {
    this.print(x);
    this.newLine();
  }

  @Override
  public void println(int x) throws IOException {
    this.print(x);
    this.newLine();
  }

  @Override
  public void println(long x) throws IOException {
    this.print(x);
    this.newLine();
  }

  @Override
  public void println(float x) throws IOException {
    this.print(x);
    this.newLine();
  }

  @Override
  public void println(double x) throws IOException {
    this.print(x);
    this.newLine();
  }

  @Override
  public void println(char[] x) throws IOException {
    this.print(x);
    this.newLine();
  }

  @Override
  public void println(String x) throws IOException {
    this.print(x);
    this.newLine();
  }

  @Override
  public void println(Object x) throws IOException {
    this.print(x);
    this.newLine();
  }

  @Override
  public void clear() throws IOException {
    underlying.clear();
  }

  @Override
  public void clearBuffer() throws IOException {
    underlying.clearBuffer();
  }

  @Override
  public void flush() throws IOException {
    this.escapingWriter.flush();
  }

  @Override
  public void close() throws IOException {
    this.escapingWriter.close();
    this.underlying.close();
  }

  @Override
  public int getRemaining() {
    return this.underlying.getRemaining();
  }

  @Override
  public void write(String s) throws IOException {
    this.write(s, 0, s.length());
  }

  @Override
  public void write(String s, int off, int len) throws IOException {
    this.escapingWriter.write(s, off, len);
  }

  @Override
  public void write(char[] cbuf) throws IOException {
    this.write(cbuf, 0, cbuf.length);
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    this.escapingWriter.writeSafe(cbuf, off, len);
  }

  @Override
  public void write(int cp) throws IOException {
    this.escapingWriter.write(cp);
  }

  @Override
  public void writeKnownSafeContent(char[] s) throws IOException {
    this.escapingWriter.writeSafe(s, 0, s.length);
  }

  @Override
  public void writeKnownSafeContent(String s) throws IOException {
    this.escapingWriter.writeSafe(s);
  }

  /**
   * isSoft returns whether this writer attempts to interoperate with systems
   * that HTML escape inputs by default before they reach this writer.
   * <p>
   * It treats unsafe content in HTML text and attribute contexts as partially
   * escaped HTML instead of as plain text.
   * <p>
   * For example, when not soft, interpolating the string
   * {@code "foo&amp <bar>"} into an HTML text content will result in writing
   * {@code "foo&amp;amp &lt;bar&gt;"} but when soft will result in writing
   * {@code "foo&amp &lt;bar&gt;"} -- existing entities are not reencoded.
   */
  public boolean isSoft() {
    return this.escapingWriter.isSoft();
  }

  /**  */
  public void setSoft(boolean b) {
    this.escapingWriter.setSoft(b);
  }
}
