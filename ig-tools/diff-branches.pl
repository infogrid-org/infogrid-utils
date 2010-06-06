#!/usr/bin/perl
#
# Diff two branches and spit out differences
#

use strict;

my $first=$ARGV[0];
my $second=$ARGV[1];

unless( $first && $second ) {
    print STDERR "Synopsis: diff-branches.pl <first branch> <second branch>\n";
    exit 1;
}
unless( -r $first ) {
    print STDERR "Cannot read file/directory $first.\n";
    exit 1;
}
unless( -r $second ) {
    print STDERR "Cannot read file/directory $second.\n";
    exit 1;
}

# Print out a statement that can be excuted
print "# The following commands must be executed to make branch ${second} look the same as branch ${first}:\n";

# Don't look in certain directories
my @result = `diff -r -q -x .svn -x private -x build -x dist -x upload -x .DS_Store -x genfiles.properties "${first}" "${second}"`;
foreach my $line ( @result ) {
    $line =~ s/\s+$//g;
    if( $line =~ m/^Files (.*) and (.*) differ$/ ) {
        print "cp $1 $2 # differ\n";
    } elsif( $line =~ m/^Only in (.*): (.*)$/ ) {
        my $relativePath = $1;
        my $file         = $2;
        if( $relativePath =~ m/^$first\// ) {
            $relativePath =~ s/^$first\///g;
            print "cp $first/$relativePath/$file $second/$relativePath/$file # only in $first\n";
        } else {
            print "# Cannot deal with file that is only in $relativePath: $file\n";
        }
    } else {
        print "# Unexpected line: $line\n";
    }
}
exit 0;

