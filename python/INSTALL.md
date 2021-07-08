Installing the Python interface to SpatialKappa
===============================================

0. Build the SpatialKappa jar as described in [../README.md][1]

1. Either:
   
   a. Install py4j using `pip`:

        sudo apt-get install python-pip 
        sudo pip install py4j --upgrade

      To install in a non-standard location, use the `-t` flag, e.g.:
   
        pip install py4j --upgrade -t ${HOME}/lib/python2.7/site-packages/

   b. Install py4j with python 2.7 from source:

        git clone https://github.com/davidcsterratt/py4j
        cd py4j
        cd py4j-java
        ant jar
        cd ../py4j/py4j-python
        sudo python2.7 setup.py install

   
2. Install SpatialKappa python files
   
        sudo python2.7 setup.py install

    An alternative prefix can be specified like this:

        python2.7 setup.py install --prefix=${HOME}

3. Run the tests:

        python2.7 -m unittest test
   
   This gives an indication of what methods are exposed to python.

[1]: ../README.md
