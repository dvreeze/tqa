
Directories folder1 and folder2 together should contain the specific NT12 files.

The ZIP file of core files should contain the non-NT12-specific www.nltaxonomie.nl files, along with www.xbrl.org and www.w3.org.

The remaining-files directory emulates files that are not found anywhere but are "retrieved from the internet".

Directory folder1 happens to contain only 2 files in www.nltaxonomie.nl/nt12/ez/20170714.a/validation, and a 3rd one in www.nltaxonomie.nl/nt12/ez/20170714.a/presentation.
The first 2 of these 3 files are not found anywhere else, so must be resolved in folder1.

Directory folder2 contains the NT12 files, except for the 2 www.nltaxonomie.nl/nt12/ez/20170714.a/validation ones and the entrypoint file.

The ZIP file is as described above, but misses the xbrl-instance-2003-12-31.xsd file, which instead can be found in the remaining-files directory tree. All needed files
not found elsewhere are in this directory tree, emulating retrieval from the internet.

