ex09-handin.pdf: ex09-handin.aux bach.bbl
	xelatex -shell-escape ex09-handin.tex

ex09-handin.aux bach.bcf: ex09-handin.tex
	xelatex -shell-escape ex09-handin.tex

bach.bbl: bach.bcf literatur.bib
	biber bach.bcf


.PHONY: clean
clean:
	rm *.aux *.log *.out *.nav *.snm *.toc *.bcf *.blg ex09-handin.pdf
