#!/usr/bin/perl
#
# Create an InfoGrid graphdb web project that is a clone of MeshWorld but with different name
# and in the local directory
#

use strict;
use Getopt::Long;
use Perl6::Slurp;

my $projectName;
my $igPath='../..'; # default is a sibling of MeshWorld
my $help;

my $parseOk = GetOptions(
        "name=s",         \$projectName,
        "infogridpath=s", \$igPath,
        "help",           \$help );

if( !$parseOk || @ARGV || !$projectName || $help ) {
    synopsis();
}
if( $projectName !~ m/^[a-z]+\.[-a-z0-9]+\.[-_a-zA-Z0-9\.]+$/i ) {
    die <<END;
ERROR: projectname must be a valid Java package name using the reverse domain name convention,
       e.g. com.example.foobar.www
END
}

if( ! -d "$igPath/ig-ui/testapps/org.infogrid.meshworld/nbproject" ) {
    die <<END;
ERROR: Cannot find a valid InfoGrid tree at path $igPath
END
}

my $meshWorld = "$igPath/ig-ui/testapps/org.infogrid.meshworld";

mkdir $projectName || die( "Cannot create directory $projectName" );

# We can't simply do things recursively because src/org/infogrid/meshworld/AppInitializationFilter.java needs
# to be cloned to src/com/example/some/where/else/AppInitializationFilter.java

cloneFile( "$meshWorld/build.xml", "$projectName/build.xml" );

my $path = '';
foreach my $dir ( split( '/', 'src/java/org/infogrid' )) {
    $path .= "/$dir";
    mkdir "$projectName$path" || die( "Cannot create directory $projectName$path" );
}

my $path = '/src';
foreach my $dir ( split( '\.', "java.$projectName" )) {
    $path .= "/$dir";
    unless( -d "$projectName$path" ) {
        mkdir "$projectName$path" || die( "Cannot create directory $projectName$path" );
    }
}
my $packageDir = $projectName;
$packageDir =~ s/\./\//g;

cloneDirectory( "$meshWorld/src/java/org/infogrid/meshworld", "$projectName/src/java/$packageDir" );

foreach my $dir ( 'infogrid-moduleads', 'nbproject', 'web', 'src/java/org/infogrid/jee' ) {
    cloneDirectory( "$meshWorld/$dir", "$projectName/$dir" );
}

##
sub cloneDirectory {
    my $fromDir = shift;
    my $toDir   = shift;

    unless( -d $toDir ) {
        mkdir $toDir || die( "Cannot create directory $toDir" );
    }

    my @fromFiles = grep { !/\/\.{1,2}$/ } <$fromDir/*>;
    foreach my $fromFile ( @fromFiles ) {
        my $toFile = $fromFile;
        $toFile =~ s/^$fromDir//;
        if( -d $fromFile ) {
            cloneDirectory( $fromFile, "$toDir$toFile" );
            next;
        }
        cloneFile( $fromFile, "$toDir$toFile" );
    }
}

##
sub cloneFile {
    my $fromFile = shift;
    my $toFile   = shift;

    my $content = slurp( $fromFile );

    $content =~ s/org\.infogrid\.meshworld/$projectName/g;
    $content =~ s/org\/infogrid\/meshworld/$packageDir/g;
    $content =~ s/Mesh\s*World/Unnamed App/g;

    writeFile( $toFile, $content );
}

##
sub writeFile {
    my $filename = shift;
    my $content  = shift;

    if( -e $filename ) {
        die( "ERROR: File $filename exists already. Won't overwrite." );
    }

    open F, ">$filename" || die( "ERROR: Cannot write to file $filename" );
    print F $content;
    close F;

    1;
}

##
sub synopsis {
    print STDERR <<END;
Synopsis:
    $0 --name <projectname> [ --infogridpath <infogridpath> ] [ --help ]
where:
    projectname:  name of the model project, fully qualified, e.g. com.example.model.Foobar
    infogridpath: relative path to the InfoGrid tree in the version to be used,
                  e.g. ../../svn.infogrid.org/infogrid/trunk. Defaults to ../..
END
    exit 1;
}

1;

