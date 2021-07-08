Steps to distribute SpatialKappa python package
===============================================

Testing in Test PyPi repository
-------------------------------

1. Test package
```
python2 setup.py test
```

2. Build package
```
rm -Rf build/*
rm -Rf dist/*
python setup.py sdist bdist_wheel
```

3. Upload to the Test PyPi repository
```
twine upload --repository-url https://test.pypi.org/legacy/ dist/*
```

4. Create virtual environment https://packaging.python.org/tutorials/installing-packages/#creating-virtual-environments
```
virtualenv SpatialKappa
source SpatialKappa/bin/activate
PYTHONPATH=
```

5. Test in virtual environment
```
python -m pip install --index-url https://test.pypi.org/simple/ --extra-index-url=https://pypi.org/simple/  SpatialKappa==2.1.5
python -m unittest SpatialKappa.tests

```

Distributing in main PyPi repository
------------------------------------

1. Change `POSTFIX` in `setup.py` to `.rc1` (say)

2. Test and rebuild

```
python2 setup.py test
rm -f dist/*
python setup.py sdist bdist_wheel
```

3. Upload
```
twine upload  dist/*
```
