import setuptools

VERSION = '2.1.5'
POSTVERSION = ''

with open("README.md", "r") as fh:
    long_description = fh.read()

setuptools.setup(
    name="SpatialKappa",
    version=VERSION + POSTVERSION,
    author="Donal Stewart, Anatoly Sorokin, David C Sterratt",
    author_email="david.c.sterratt@ed.ac.uk",
    description="Implementation of syntactic extension of Kappa to allow expression of location and transport in models",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/davidcsterratt/SpatialKappa",
    packages=setuptools.find_packages(),
    install_requires=['py4j'],
    classifiers=(
        "Programming Language :: Python :: 2",
        "Programming Language :: Java",
        "License :: OSI Approved :: GNU General Public License v3 (GPLv3)",
        "Operating System :: OS Independent",
    ),
    python_requires='<3',
    package_data={'SpatialKappa': ['share/SpatialKappa/*', 'tests/*.ka']},
    test_suite='SpatialKappa.tests')
