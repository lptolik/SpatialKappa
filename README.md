SpatialKappa
============

<p style="text-align: center;">14 September 2012</p>

<p style="text-align: center;">Copyright &copy; 2012 DemonSoft.org
&lt;<a
href="http://www.demonsoft.org/">http://www.demonsoft.org/</a>&gt;<br/>
Copyright &copy; 2014-2019 David C Sterratt <david.c.sterratt@ed.ac.uk>
</p>

INTRODUCTION
------------

This project contains an implementation of a syntactic extension
of Kappa to allow expression of location and transport in models. A user manual is available [here][manual-link].

Feel free to add bug reports or feature requests [here][bug-link].

INSTALLATION
------------

Either 

1. Run the standalone jar file (SpatialKappa-v2.1.3.jar)
2. Install the python interface using pip:
```pip install SpatialKappa``` and then test using ```python -m unittest SpatialKappa.tests```. Within python ```help(SpatialKappa)``` gives the list of model-building and control functions.


COMPILATION
-----------

To make the `SpatialKappa.jar` file either:

1. Import this code as an Ecclipse project; or
2. Install ant (`sudo apt install ant openjdk-7-jdk` on Debian/Ubuntu) and run `make all`

To install the python interface, cd into the python directory, and
follow the instructions in [python/INSTALL.md](python/INSTALL.md)

LICENSE
-------

This project is licenced under the GNU Lesser General Public
Licence Version 3 (LGPLv3), included as [LICENSE.html](LICENSE.html).

Included third party libraries are released under their respective
licences, listed below:

<table style="border: groove;">
	<tr>
		<th>Library</th>
		<th>Home site</th>
		<th>License</th>
	</tr>
	<tr>
		<td>Antlr v3.2</td>
		<td><a href="http://www.antlr.org">http://www.antlr.org</a></td>
		<td><a href="http://www.antlr.org/license.html">http://www.antlr.org/license.html</a></td>
	</tr>
	<tr>
		<td>jlfgr v1.0</td>
		<td><a
			href="http://java.sun.com/developer/techDocs/hi/repository/">http://java.sun.com/developer/techDocs/hi/repository/</a></td>
		<td>Included in archive</td>
	</tr>
	<tr>
		<td>commons io v1.4</td>
		<td><a href="http://commons.apache.org/io/">http://commons.apache.org/io/</a></td>
		<td><a href="http://commons.apache.org/io/license.html">http://commons.apache.org/io/license.html</a></td>
	</tr>
	<tr>
		<td>jcommon v1.0.16</td>
		<td><a href="http://www.jfree.org/jcommon/">http://www.jfree.org/jcommon/</a></td>
		<td><a href="http://www.gnu.org/licenses/lgpl.html">http://www.gnu.org/licenses/lgpl.html</a></td>
	</tr>
	<tr>
		<td>jfreechart v1.0.13</td>
		<td><a href="http://www.jfree.org/jfreechart/">http://www.jfree.org/jfreechart/</a></td>
		<td><a href="http://www.gnu.org/licenses/lgpl.html">http://www.gnu.org/licenses/lgpl.html</a></td>
	</tr>
	<tr>
		<td>EasyMock v3.0</td>
		<td><a href="http://easymock.org/">http://easymock.org/</a></td>
		<td><a href="http://easymock.org/License.html">http://easymock.org/License.html</a></td>
	</tr>
	<tr>
		<td>CgLib v2.2</td>
		<td><a href="http://cglib.sourceforge.net/">http://cglib.sourceforge.net/</a></td>
		<td><a href="http://www.apache.org/foundation/licence-FAQ.html">http://www.apache.org/foundation/licence-FAQ.html</a></td>
	</tr>
	<tr>
		<td>Objenesis v1.2</td>
		<td><a href="http://objenesis.googlecode.com/svn/docs/index.html">http://objenesis.googlecode.com/svn/docs/index.html</a></td>
		<td><a href="http://objenesis.googlecode.com/svn/docs/license.html">http://objenesis.googlecode.com/svn/docs/license.html</a></td>
	</tr>
	<tr>
		<td>ASM v3.3.1</td>
		<td><a href="http://asm.ow2.org/index.html">http://asm.ow2.org/index.html</a></td>
		<td><a href="http://asm.ow2.org/license.html">http://asm.ow2.org/license.html</a></td>
	</tr>
</table>


ACKNOWLEDGEMENTS
----------------

The development of Spatial Kappa was funded in part by <a href="http://www.csbe.ed.ac.uk/">SynthSys</a>. SynthSys is a Centre for Integrative Systems Biology (CISB) funded by BBSRC and EPSRC, reference BB/D019621/1.

<img width=200px align="left" alt="EU flag" src="docs/figs/flag_yellow_low.jpg"/>

This open source software code was developed with funding from the
European Union Seventh Framework Programme (FP7/2007-2013) under grant
agreement nos. 241498 (EUROSPIN project), 242167 (SynSys-project) and
604102 (Human Brain Project), and in the Human Brain Project, funded
from the European Union’s Horizon 2020 Framework Programme for
Research and Innovation under Specific Grant Agreements No. 720270 and
No. 785907 (Human Brain Project SGA1 and SGA2).


DISCLAIMER
----------	

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

[bug-link]: https://github.com/lptolik/SpatialKappa/issues
[manual-link]: https://github.com/lptolik/SpatialKappa/raw/master/docs/manual/SpatialKappaManual-v2.1.0.pdf
[1]: python/INSTALL.md
