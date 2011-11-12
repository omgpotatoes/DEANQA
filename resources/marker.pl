#!/usr/local/bin/perl

# Usage: ./marker.pl input_filename answer_filename tag file_extension
#    where:
#        - input_filename : the name of the file you use as input to your project
#        - answer_filename : an answer key style file
#        - tag : the tag that will be used to tag answers in the story
#        - file_extension : the annotated stories will be saved to the same file name but with this extension
# Description: reads every story from the input file and tags the asnwers with
#   the tag provided as parameter. The new story is saved in the same file but
#   with .file_extension extension.


#Checks it has enough params
if ((scalar @ARGV)<4) {
  die "Usage: ./marker.pl input_filename answer_filename tag file_extension\n";
}
$inputfn = shift;
$ansfn = shift;
$tag = shift;
$newextension = shift;

#Reads the answer key
$anskeystr = readWholeFile($ansfn);

open(INPUT, "< $inputfn") or die "Can not read input file $inputfn.\n";
#Reads and fixed the path
$path = <INPUT>;
chomp $path;
if (not ($path =~ /\/$/)) {
  $path .= '/';
}
#Reads each story
while (<INPUT>) {
  chomp;
  $storyfn = $_;
  #Finds the answers for this story
  %answers = getAnswers($anskeystr, $storyfn);
  if (scalar %answers) {
    #Reads the story and process it
    $fullstoryfn = $path.$storyfn;
    if (open(STORY, "< $fullstoryfn")) {
      @story = <STORY>;
      close(STORY);
      foreach $qno (keys %answers) {
	foreach $aline (split /\s*,\s*/, $answers{$qno}) {
	  $line = $story[$aline-1];
	  chomp $line;
	  $story[$aline-1] = "<$tag$qno>".$line."</$tag$qno>\n";
	}
      }
      #Prints the new story
      $storyfn =~ /^([^.]+)\./;
      $newstoryfn = $path.$1.".".$newextension;
      if (open(STORY, "> $newstoryfn")) {
	print STORY @story;
	close(STORY);
	print "File $storyfn successfully annotated.\n";
      } else {
	print "ERROR: Can not write to $newstoryfn.\n";
      }
    } else {
      print "ERROR: Can not read story $fullstoryfn.\n";
    }
  } else {
    print "WARNING: No answers found for story $storyfn.\n";
  }
}
close(INPUT);


sub readWholeFile {
  my $fn = shift;
  my $result;
  my @content;
  open(RWF_INPUT, "< $fn") or die "ERROR: Failed to open $fn.\n";
  @content = <RWF_INPUT>;
  $result = join('', @content);
  return $result;
}

#Returns a hash with all answers for a specific story
sub getAnswers {
  my $anskeystr = shift;
  my $fn = shift;
  my %answers = ();
  my $i;
  #Gets rid of the file extension
  $fn =~ /^([^.]+)\./;
  $fn = $1;
  if ($anskeystr =~ /<FILE>$fn\.(((?!<\/FILE>)(.|\n))*)<\/FILE>/i) {
    my $qastr = $1;
    my @qa = ($qastr =~ /<Q_NUMBER>(.*)\n<A_LINE>(.*)\n/gi);
    for($i=0; $i<=$#qa; $i+=2) {
      my $q = trimUnnecSpaces($qa[$i])+0;
      my $a = trimUnnecSpaces($qa[$i+1]);
      if ($q ne "") {
	$answers{$q} = $a;
      }
    }
  }
  return %answers;
}

sub trimUnnecSpaces {
  my $string = shift;
  $string =~ s/\s{2,}/ /g;
  $string =~ s/^\s//;
  $string =~ s/\s$//;
  return $string;
}
