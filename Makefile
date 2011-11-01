#=============================
# variables and configuration
#=============================
SHELL = /bin/bash

JAVAC = javac

MAIN = DeanQA

SRC_DIR = src
BIN_DIR = bin
LIB_DIR = lib

PACKAGE = cs2731

# classpath
CP = $(LIB_DIR)/*:$(CLASSPATH)

DEBUGFLAGS = -g -ea
RUNFLAGS = 

# arguments
ARGS = arg1 arg2

#.SUFFIXES: .java .class

.PHONY : all clean

#=============================
#  build targets  
#  $@    = Current target
#  $*    = Current target without extension
#  $<    = Current dependency
#=============================

all: build

build:
	if [ ! -d bin ]; then mkdir bin; fi
	$(JAVAC) -sourcepath $(SRC_DIR) -d $(BIN_DIR) -cp $(CP) src/$(PACKAGE)/$(MAIN).java

run: build
	java -cp $(BIN_DIR):$(CP) $(RUNFLAGS) $(PACKAGE).$(MAIN) $(ARGS)

debug: build
	java -cp $(BIN_DIR):$(CP) $(DEBUGFLAGS) $(PACKAGE).$(MAIN) $(ARGS)

report:
	pdflatex report.tex

#=============================
#  other targets  
#=============================

# removes all classfiles
# and the bin directory
clean:
	rm -f bin/*
#rm -rf report.aux report.log

rebuild: clean build

