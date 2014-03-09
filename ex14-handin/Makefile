1864-ex11.pdf: 1864-ex11.aux 1864-ex11.bbl
	xelatex -shell-escape 1864-ex11.tex

1864-ex11.aux 1864-ex11.bcf: 1864-ex11.tex
	xelatex -shell-escape 1864-ex11.tex

1864-ex11.bbl: 1864-ex11.bcf literatur.bib
	biber 1864-ex11.bcf


.PHONY: clean
clean:
	rm *.aux *.log *.out *.nav *.snm *.toc *.bcf *.blg *.bbl *.run.xml 1864-ex11.pdf
