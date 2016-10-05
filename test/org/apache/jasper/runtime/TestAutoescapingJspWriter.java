package org.apache.jasper.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

import org.apache.catalina.startup.TomcatBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("javadoc")
public final class TestAutoescapingJspWriter extends TomcatBaseTest {

  StringWriter backingBuffer;
  AutoescapingJspWriter writer;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    final int bufferSize = 8192;
    backingBuffer = new StringWriter(bufferSize);

    ServletResponse response = new ServletResponse() {
      // TODO: figure out how the Tomcat project mocks things.
      PrintWriter pw = new PrintWriter(backingBuffer);

      @Override
      public String getCharacterEncoding() {
        return "UTF-8";
      }

      @Override
      public String getContentType() {
        return "text/html";
      }

      @Override
      public ServletOutputStream getOutputStream() throws IOException {
        throw new AssertionError("Testing text output");
      }

      @Override
      public PrintWriter getWriter() throws IOException {
        return pw;
      }

      @Override
      public void setCharacterEncoding(String charset) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void setContentLength(int len) {
        // Thanks
      }

      @Override
      public void setContentLengthLong(long length) {
        // Thanks
      }

      @Override
      public void setContentType(String type) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void setBufferSize(int size) {
        // Ignore
      }

      @Override
      public int getBufferSize() {
        return bufferSize;
      }

      @Override
      public void flushBuffer() throws IOException {
        // Is in-memory.
      }

      @Override
      public void resetBuffer() {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean isCommitted() {
        return false;
      }

      @Override
      public void reset() {
        throw new UnsupportedOperationException();
      }

      @Override
      public void setLocale(Locale loc) {
        // Thanks
      }

      @Override
      public Locale getLocale() {
        return Locale.ROOT;
      }

    };
    JspWriterImpl writerImpl = new JspWriterImpl(response, bufferSize, false);

    this.writer = new AutoescapingJspWriter(writerImpl);
    this.writer.setSoft(false);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    this.writer = null;
    this.backingBuffer = null;
  }

  @Test
  public void testWriterEscapesHtmlText() throws IOException {
    writer.writeKnownSafeContent("<div>");
    writer.write("I <3 HTML");
    writer.writeKnownSafeContent("</div>");
    writer.close();

    assertEquals(
        "<div>I &lt;3 HTML</div>",
        this.backingBuffer.toString());
  }

  @Test
  public void testWriterEscapesHtmlTextChars() throws IOException {
    writer.writeKnownSafeContent("<div>".toCharArray());
    writer.write("I <3 HTML".toCharArray());
    writer.writeKnownSafeContent("</div>".toCharArray());
    writer.close();

    assertEquals(
        "<div>I &lt;3 HTML</div>",
        this.backingBuffer.toString());
  }

  @Test
  public void testWriterFiltersUrls() throws IOException {
    writer.writeKnownSafeContent("<a href=");
    writer.write("javascript:doEvil()");
    writer.writeKnownSafeContent(">link</a>");
    writer.close();

    assertEquals(
        "<a href=\"#ZautoescZ\">link</a>",
        this.backingBuffer.toString());
  }

  public static final class MyBean {
    public String getFoo() { return "foo"; }
    public int getBar() { return 42; }
    public boolean getBaz() { return true; }
  }


  @Test
  public void testEncodingOfObjectsInScript() throws IOException {
    writer.writeKnownSafeContent("<script>var o = ");
    writer.print(new MyBean());
    writer.writeKnownSafeContent(";</script>");
    writer.close();

    assertEquals(
        "<script>var o = {"
        +   "'bar':42,"
        +   "'baz':true,"
        +   "'class':'class " + MyBean.class.getName() + "',"
        +   "'foo':'foo'"
        + "};</script>",
        this.backingBuffer.toString());
  }


  @Test
  public void testAllPrintAndWriteOverridden() throws Exception {
    // All write and print methods should be intercepted and reinterpreted
    // before reaching the underlying buffer.
    for (Method m : AutoescapingJspWriter.class.getMethods()) {
      String name = m.getName();
      if (name.startsWith("print") || name.startsWith("write")) {
        if (m.getDeclaringClass() != AutoescapingJspWriter.class) {
          // TODO: there must be some way to get a method descriptor
          // from a Method
          fail(
              "Not overridden: "
              + m.getDeclaringClass().getName() + "." + m.getName()
              + "(" + Arrays.toString(m.getParameterTypes()));
        }
      }
    }
  }
}
