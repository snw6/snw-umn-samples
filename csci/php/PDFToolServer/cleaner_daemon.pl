#!/usr/bin/perl -w
use strict;
use warnings;

use POSIX;
use Cwd 'abs_path';
use File::Basename qw( dirname );

use Term::ANSIColor;
use Getopt::Long;
use Pod::Usage;

use File::Find::Rule;

use constant {
    HOURS_DEF         =>        12,
    SLEEP_TIME_DEF    =>        12 * 60 * 60,
    LOG_FILE_DEF      =>        "./cleaner_daemon.log"
};

=pod
=head1 NAME

    cleaner_daemon.pl
    
    Erases files that are older than a threshold.
    Executed as a typical daemon.
    

=head1 SYNOPSIS

    cleaner_daemon.pl [-h|-t]

=head1 OPTIONS

    -t, --time          HOURS       Number of hours after which the file is considered as expired.
    -s, --sleep         HOURS       Sleep time before the next iteration.
    -d, --directory     PATH        The folder that will be checked for outdated files.
    -l, --logfile       PATH        User-specific log file.
    -h, --help_opt                  This synopsis.

    Default values: "-h 12 -d ./"
=cut

sub Log {
    my ($message, $log_file) = @_;
    warn "[error] [func:Log] Log file was not specified!\n"
        if (not defined $log_file);
    warn "[error] [func:Log] Message was not specified!\n"
        if (not defined $message);
    
    print $log_file localtime(time) . " -- $message\n";
}


my ($help_opt, $time_opt, $dir_opt, $log_opt, $sleep_opt) = (undef, undef, undef, undef, undef);
my $iteration_global = 0;
my $interrupted = 0;
GetOptions(
    'h|help'        => \$help_opt,
    's|sleep'        => \$sleep_opt,
    'd|directory=s' => \$dir_opt,
    't|time=i'      => \$time_opt,
    'l|logfile=s'    => \$log_opt,
);
pod2usage(-verbose => 1) and exit if $help_opt;
Log("cleaner_daemon started. PID=$$.", *STDERR);

# saving the original path to the script
my $scipt_dir = dirname(abs_path($0)) . '/'; 

# daemonizing
#my $pid = fork ();
#if ($pid < 0) {
#    die "fork: $!";
#} elsif ($pid) {
#    exit 0;
#}
#chdir "/";
#umask 0;

# assigning the default values
$time_opt = HOURS_DEF
    if not defined $time_opt;

$dir_opt = $scipt_dir
    if not defined $dir_opt;

$log_opt = LOG_FILE_DEF
    if not defined $log_opt;

$sleep_opt = SLEEP_TIME_DEF
    if not defined $sleep_opt;

# processing logs
open LOGS, ">>$log_opt"
    or warn "[error] Failed to open log file $log_opt! $!\n\tUsing STDERR instead.\n";
$log_opt = (defined *LOGS) ? *LOGS : *STDERR;



Log("Started. PID=$$. Current arguments:\n\ttime_opt=$time_opt\n\tdir_opt=$dir_opt\n\tlog_opt=$log_opt\
    sleep_opt=$sleep_opt", $log_opt);

while (not $interrupted)  {
    # logging
    ++$iteration_global;
    Log("Awaken. This is my $iteration_global iteration.", $log_opt);

    # calculating the current limit in seconds in UNIX format
    my $threshold_in_secs = time() - $time_opt * 60 * 60;

    # searching for candidates to remove
    Log("Started to search for the new candidates.", $log_opt);
    my @files_to_delete = File::Find::Rule->file()
        ->mtime("<=$threshold_in_secs")
        ->in("$dir_opt");

    # finally remove them
    Log("No candidates found! All files look good.", $log_opt)
        if ($#files_to_delete < 0);

    for (@files_to_delete) {
        Log(localtime(time) . " -- Removing file $_\n", $log_opt);
        unlink $_;
    }

    # suspending
    Log("Going to sleep for $sleep_opt seconds. Chao.", $log_opt);
    flush $log_opt;
    sleep $sleep_opt;
}

Log("Died.", $log_opt);
close $log_opt;

__END__

