PREFIX=/usr/local

build_py:
	cd .. ; make
	python2.7 setup.py build

test: build_py
	python2.7 -m unittest tests

install: build_py
	python2.7 setup.py install --prefix=$(PREFIX)

