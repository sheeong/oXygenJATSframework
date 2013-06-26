
XSLT combine.xsl combines schmatron files.
To merge more than one files, supply the parameter 'files' with a comma separated list of files. The files must be in this directory.

Example:
saxon.Transform metadata.sch combine.xsl files=tables.sch,tables2.sch,tables3.sch


