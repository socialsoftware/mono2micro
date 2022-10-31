$1 == "commit" {commit = $2; time = $3; author = $4}
$1 == "A" {printf "%s;ADDED; ;%s;%s;%s\n", commit, $2, time, author}
$1 == "M" {printf "%s;MODIFIED; ;%s;%s;%s\n", commit, $2, time, author } 
$1 == "D" {printf "%s;DELETED; ;%s;%s;%s\n", commit, $2, time, author }
match($1,"R[0-9][0-9][0-9]") {printf "%s;RENAMED;%s;%s;%s;%s\n", commit, $2, $3, time, author} 
