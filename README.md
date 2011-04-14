[JPT] (http://archimedeanco.com/jpt/) - Java Page Templates
===========================================================

JPT is a powerful presentation layer technology that allows business stakeholders, designers, and developers to prototype, and refine documents and web pages with ease.

XHTML documents stay flexible, easy to use, by keeping the templating information inside the tag attributes.

JPT is a Java implementation of Zope Page Templates ZPT, which generates documents in a manner similar to XML and XSLT.

JPT/ZPT compared to XSLT:
------------------------

* templates have direct access to data objects and don't need an intermediate layer of xml
* templates parse as html, making life much easier for non-programming designers who need to edit the template. 


Some links
==========
    [Zope Homepage](http://www.zope.org/)
    [An introduction to ZPT](http://zope.org/Documentation/Books/ZopeBook/2_6Edition/ZPT.stx)
    [The ZPT reference](http://www.zope.org/Documentation/Books/ZopeBook/2_6Edition/AppendixC.stx)

One of the ways JPT is powerful is in it's simplicity; the context against which the template is processed may be any arbitrary Java object. 
A path in a path expression corresponds to a traversal of that object's properties and methods, found using bean introspection and reflection respectively. 
This allows powerful direct access by the template of any arbitrarily complex graph of business objects, without having to prepare those objects in any special way for rendering. 
Any public bean property or method of any arbitrary object may be accessed. 

At the time of this writing all "tal" and "metal" attributes are implemented, with the exception of "tal:on-error". 
Due to differences between doing template processing inside the Zope framework in python and doing them in Java, the expression syntax, TALES, and path expressions in particular, are a bit different from ZPT. 
Differences between ZPT and JPT are discussed in the user's guide inside the doc folder.
