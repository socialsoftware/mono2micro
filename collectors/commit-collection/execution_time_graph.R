library(ggplot2)

execution_times <- read.csv("/home/joaolourenco/Thesis/development/scripts/resources/codebases_collection/execution_times.csv")
colnames(execution_times) <- c("codebase", "time", "commits")
ggplot(execution_times, aes(x=commits, time)) +
  geom_point()
