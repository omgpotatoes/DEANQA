# Makefile for a paper using pdflatex

# The key files we need.   You should update TEX, BIB, and FIGS.
PDF = main.pdf 
PS = main.ps 

TEX =	*.tex
BIB =	references.bib 
OTHERS = 
FIGS = 
# If the FIGS are generated from an eps source, 
# update the "clean" rule below to rm $(FIGS).
# The following line helps keep them around until an explicit "make clean."
.PRECIOUS: $(FIGS)

.PHONY: all print wordcount spell clean showlogs 

#  The main target is pdf
all: $(PDF)

pretty: $(PDF)
	echo "removing other files !!!"
	-rm -f *.aux *.bbl *.blg *.log *.out TAGS words
	-rm -f *~ 

print: $(PS)
	lpr $(PS)

words: $(TEX)
	detex -l -n -w $(TEX) > $@

wordcount: words
	wc -w < words

# okwords is a file containing a sorted list of words that are not misspelled
spell: words okwords
	spell words | sort -u | comm -23 - okwords

# List the dependencies of this PDF on all of the inputs.
$(PDF): $(TEX) $(BIB) $(FIGS) $(OTHERS)

# A general rule for making a PDF file from a latex file.
%.pdf: %.tex 
	pdflatex $*
	bibtex $*
	pdflatex $*
	pdflatex $*
	@echo ============== LaTeX errors =========================
	@-grep Warning:  $*.log || true
	@echo ============== BibTeX errors ========================
	@-grep Warning-- $*.blg || true
	@echo =====================================================

showlogs:
	@echo ""
	@echo " ----------------------------------------"
	@echo " Warnings from all input files:"
	-grep Warning *.log *.blg
	@echo " ----------------------------------------"

# A general rule for making a pdf version of eps figures.
%.pdf: %.eps
	epstopdf $<

# If you really need postscript, use pdf2ps or pdftops
%.ps: %.pdf
	pdftops $< $@

# extract text-only from latex file
%.txt: %.tex
	delatex $< > $@

TAGS: $(TEX) $(BIB)
	etags $(TEX) $(BIB)

# Uncomment the last line if $(FIGS) includes only pdf files that are 
# derived automatically from eps files (as is true in this template).
clean:
	rm -f *.aux *.bbl *.blg *.log *.out TAGS words
	rm -f $(PDF) $(PS) 
	rm -f *~ 
#	rm -f $(FIGS)

open:	$(PDF)
	open main.pdf &
