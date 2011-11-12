#!/usr/bin/perl

# Usage: ./grader.pl input_filename answer_key_filename answer_filename
#    where:
#        - input_filename : the name of the file you use as input to your project
#        - answer_key_filename : the CORRECT answer key file
#        - answer_filename : an answer key style file (your answers)
# Description: for every story from the input file compares the correct answer (from the first answer file) with your answer (from the second answer file) and outputs to STDOUT the diferences and statistics


#Checks it has enough params
if ((scalar @ARGV)<3) {
  die "Usage: ./grader.pl input_filename answer_key_filename answer_filename\n";
}
$inputfn = shift;
$crtansfn = shift;
$ansfn = shift;

#Reads the answer keys
$anskeystr = readWholeFile($ansfn);
$crtanskeystr = readWholeFile($crtansfn);

open(INPUT, "< $inputfn") or die "Can not read input file $inputfn.\n";
#Reads and fixed the path
$path = <INPUT>;
chomp $path;
if (not ($path =~ /\/$/)) {
  $path .= '/';
}
#Prepares statistic
$correct = 0;
$total = 0;
%correctByType = ('WHEN',0,'WHERE',0,'WHAT',0,'WHY',0,'WHO',0,'HOW',0,'OTHER',0);
%totalByType = ('WHEN',0,'WHERE',0,'WHAT',0,'WHY',0,'WHO',0,'HOW',0,'OTHER',0);
$byTypeStatGood = 1;
#For each story
while (<INPUT>) {
  chomp;
  $storyfn = $_;
  print "\n\n---------------------------------------------------------------\n";
  print "Processing file $storyfn...\n";
  #Finds the answers for this story
  %answers = getAnswers($anskeystr, $storyfn);
  %crtanswers = getAnswers($crtanskeystr, $storyfn);
  if (scalar %crtanswers) {
    #Reads the story and process it
    $fullstoryfn = $path.$storyfn;
    if (open(STORY, "< $fullstoryfn")) {
      $storyAvail = 1;
      @story = <STORY>;
      unshift(@story, '');
      $storystr = join('',@story);
      close(STORY);
    } else {
      $storyAvail = 0;
      $byTypeStatGood = 0;
      print "WARNING: Can not read the story from file $fullstoryfn. Text description will not be available for this story.\n"
    }
    foreach $qno (sort(keys %crtanswers)) {
      print "\nQUESTION $qno\n";
      $total++;
      if (($storyAvail) and ($storystr =~ /<Q$qno>(.*)\n/i)) {
	print "    ".$1."\n";
	$qtype = getQuestionType($1);
	$totalByType{$qtype}++;
      }
      @qstCrtAns = answerToArray($crtanswers{$qno});
      @qstAns = answerToArray($answers{$qno});
      if (partOf(\@qstCrtAns,\@qstAns)) {
	print "++CORRECT (line(s) ".join(',',@qstCrtAns).")\n";
	$correct++;
	if ($storyAvail) {
	  print "      ".$story[$qstAns[0]];
	  $correctByType{$qtype}++;
	}	
      } else {
	if (scalar @qstCrtAns) {
	  print "--INcorrect (line $qstAns[0] instead of ".join(',',@qstCrtAns).")\n";
	  if ($storyAvail) {
	    print "      $story[$qstAns[0]]";
	    print "   INSTEAD OF\n";
	    print "      ".join(" -OR- ",map($story[$_], @qstCrtAns));
	  }	
	} else {
	  print "--INcorrect (question has not answer)\n";
	}
      }
    }
  } else {
    print "WARNING: No correct answers found for story $storyfn. Nothing to compare with.\n";
  }
}
close(INPUT);
#Prints statistic
print "\n\n\n\n\n";
print "******************************************************************\n";
print "Accuracy: $correct correct out of $total questions - ";
printf "%.2f",($correct/$total*100);
print "%.\n";
print "******************************************************************\n";
if ($byTypeStatGood) {
  print "Statistics by question type:\n";
  $norm = 0;
  $normcount = 0;
  foreach $qtype ('WHEN','WHERE','WHAT','WHY','WHO','HOW','OTHER') {
    printf "%-6s : %4d out of %4d - %6.2f\%.\n",$qtype,$correctByType{$qtype},$totalByType{$qtype}, $totalByType{$qtype} ? ($correctByType{$qtype}/$totalByType{$qtype}*100) : 100;
    if (($qtype ne "OTHER") and ($totalByType{$qtype}>10)) {
      $norm = $norm + $correctByType{$qtype}/$totalByType{$qtype}*100;
      $normcount++;
    }
  }
  #Normalized score
  printf "\nNORMALIZED ACCURACY : %.2f\%\n",$norm/$normcount;
  # Prints information to be put in excel files
  #Grep for "EXCEL". First line contains the correct counts and the second the total counts
  print "\n\nFor excel:\nEXCEL\t$correct";
  foreach $qtype ('WHEN','WHERE','WHAT','WHY','WHO','HOW','OTHER') {
    print "\t".$correctByType{$qtype};
  }
  print "\n";
  print "EXCEL\t$total";
  foreach $qtype ('WHEN','WHERE','WHAT','WHY','WHO','HOW','OTHER') {
    print "\t".$totalByType{$qtype};
  }
  print "\n";
} else {
  print "Statistics by question types can not be printed because at least one story was not present.\n";
}


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

sub answerToArray {
  my $string = shift;
  $string =~ s/\s//g;
  return split(/,/,$string);
}

#Test if the first element of the second array is part of the first array
sub partOf {
  my $refCrtAns = shift;
  my $refAns = shift;
  my $i;
  if (scalar @$refAns) {
    foreach $i (@$refCrtAns) {
      if ($i eq $refAns->[0]) {
	return 1;
      }
    }
  }
  return 0;
}

# A more accurate version: first keyword is reported
sub getQuestionType {
  my $str = shift;
  foreach $word (split /\s/, $str) {
    if ($word =~ /\bWHERE\b/i) {
      return 'WHERE';
    }
    if ($word =~ /\bWHEN\b/i) {
      return 'WHEN';
    }
    if ($word =~ /\bWHAT\b/i) {
      return 'WHAT';
    }
    if ($word =~ /\bWHO\b/i) {
      return 'WHO';
    }
    if ($word =~ /\bWHY\b/i) {
      return 'WHY';
    }
    if ($word =~ /\bHOW\b/i) {
      return 'HOW';
    }
  }
  return 'OTHER';
}
