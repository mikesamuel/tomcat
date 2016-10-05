Motivation
========

Given a JSP file like

```jsp
<HTML>
 <HEAD>
  <TITLE>Hello World</TITLE>
 </HEAD>
 <BODY>
  <H1>Hello World</H1>
  <a href="${param.url}">${param.text}</a>
  Today is <%= new java.util.Date().toString() %>
 </BODY>
</HTML>
```

the *jspc* tool used to produce

```java
      out.write("<HTML>\n <HEAD>\n  <TITLE>Hello World</TITLE>\n </HEAD>\n <BODY>\n  <H1>Hello World</H1>\n  <a href=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${param.url}", java.lang.String.class, (javax.servlet.jsp.PageContext)_jspx_page_context, null));
      out.write("\">");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${param.text}", java.lang.String.class, (javax.servlet.jsp.PageContext)_jspx_page_context, null));
      out.write("</a>\n  Today is ");
      out.print( new java.util.Date().toString() );
      out.write("\n </BODY>\n</HTML>\n");
```

but now it separates writes of template content from expression results:

```java
      out.writeKnownSafeContent("<HTML>\n <HEAD>\n  <TITLE>Hello World</TITLE>\n </HEAD>\n <BODY>\n  <H1>Hello World</H1>\n  <a href=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${param.url}", java.lang.String.class, (javax.servlet.jsp.PageContext)_jspx_page_context, null));
      out.writeKnownSafeContent("\">");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${param.text}", java.lang.String.class, (javax.servlet.jsp.PageContext)_jspx_page_context, null));
      out.writeKnownSafeContent("</a>\n  Today is ");
      out.print( new java.util.Date().toString() );
      out.writeKnownSafeContent("\n </BODY>\n</HTML>\n");
```

This allows hooking a custom JSP writer into the page context

```java
      out = pageContext.getOut();
      /* START HACK */
      if (Boolean.getBoolean("jasper.autoesc.enable")) {
        out = new org.apache.jasper.runtime.AutoescapingJspWriter(out);
      }
      /* END HACK */
```

which [contextually autoescapes](https://rawgit.com/mikesamuel/sanitized-jquery-templates/trunk/safetemplate.html#problem_definition)
expression results taking the burden of XSS-safety off the template authors' shoulders.

The [tests](https://github.com/mikesamuel/tomcat/blob/trunk/test/org/apache/jasper/runtime/TestAutoescapingJspWriter.java#L133) for the new `AutoescapingJspWriter` class show how a sequence of `writeKnownUnsafeContent` and regular `write` calls can safely compose HTML from trusted and untrusted content in a way that's intuitive to web developers.
