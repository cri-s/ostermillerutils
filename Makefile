JFLAGS=
JAVAC=javac
JAVA=java
JAVADOC=javadoc

all: 
	$(JAVAC) $(JFLAGS) *.java

clean:
	rm -rf doc/
	rm -f *.class
	rm -f *~
	rm -f ~*
	rm -f utils.jar

docs:
	rm -rf doc/
	mkdir doc
	$(JAVADOC) -d doc/ com.Ostermiller.util

build:
	rm -f utils.jar
	rm -f *~
	rm -f ~*
	mkdir com
	mkdir com/Ostermiller
	mkdir com/Ostermiller/util
	cp *.* Makefile com/Ostermiller/util/
	jar cfv util.jar com/
	rm -rf com/
