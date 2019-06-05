dt = $(date '+%d/%m/%Y %H:%M:%S');
git add *
git commit -am "Commit at " + "$dt" + " by Hung Nguyen 13476253"
git push origin master