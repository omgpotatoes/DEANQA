#=============================
# variables and configuration
#=============================
SHELL = /bin/bash
JAVAC = javac

MAIN = DeanQA

SRC_DIR = src
BIN_DIR = bin
LIB_DIR = lib
RES_DIR = resources
PACKAGE = cs2731

# classpath
CP = $(LIB_DIR)/*:$(CLASSPATH)

#BUILDFLAGS = -Xlint:unchecked
BUILDFLAGS =
DEBUGFLAGS = -g -ea
RUNFLAGS = 

# arguments
ARGS = $(RES_DIR)/input-train.txt output.txt
#ARGS = $(RES_DIR)/input-test1.txt output.txt

#.SUFFIXES: .java .class

.PHONY : all clean build

#=============================
#  build targets  
#  $@    = Current target
#  $*    = Current target without extension
#  $<    = Current dependency
#=============================

all: build

build:
	if [ ! -d bin ]; then mkdir bin; fi
	$(JAVAC) -sourcepath $(SRC_DIR) -d $(BIN_DIR) -cp $(CP) $(BUILDFLAGS) src/$(PACKAGE)/$(MAIN).java

run: build
	java -cp $(BIN_DIR):$(CP) $(RUNFLAGS) $(PACKAGE).$(MAIN) $(ARGS)

train: build
	java -cp $(BIN_DIR):$(CP) $(RUNFLAGS) $(PACKAGE).$(MAIN) $(RES_DIR)/input-train.txt output.txt

test: build
	java -cp $(BIN_DIR):$(CP) $(RUNFLAGS) $(PACKAGE).$(MAIN) $(RES_DIR)/input-test1.txt output.txt

debug: build
	java -cp $(BIN_DIR):$(CP) $(DEBUGFLAGS) $(PACKAGE).$(MAIN) $(ARGS)

# runs the perl grader script:
# grader.pl input_filename answerkey_filename your_answer_filename
grade:
	perl $(RES_DIR)/grader.pl $(RES_DIR)/input-train.txt $(RES_DIR)/answerkey.txt output.txt

grade2:
	perl $(RES_DIR)/grader.pl $(RES_DIR)/input-test1.txt $(RES_DIR)/answerkey-test.txt output.txt

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

