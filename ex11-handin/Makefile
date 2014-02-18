ex09-handin.pdf: ex09-handin.aux
	xelatex -shell-escape ex09-handin.tex

ex09-handin.aux: ex09-handin.tex
	xelatex -shell-escape ex09-handin.tex

.PHONY: clean
clean:
	rm *.aux *.log *.out *.nav *.snm *.toc *.bcf *.blg ex09-handin.pdf
