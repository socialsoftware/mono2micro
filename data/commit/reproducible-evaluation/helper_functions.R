initialize_analyser_data <- function(analyser_result_location) {
  all_decompositions <- read.csv(analyser_result_location,
           colClasses = c(
             "numeric","numeric","numeric","numeric","numeric","numeric","factor","numeric","numeric","numeric","numeric","factor"))
  all_decompositions$clusters <- factor(all_decompositions$clusters, levels=c("3", "4", "5", "6", "7", "8", "9", "10"))
  all_decompositions$Representation <- "temp"
  all_decompositions[all_decompositions$commit == 0 & all_decompositions$authors == 0,]$Representation <- "Sequences"
  all_decompositions[all_decompositions$authors == 100,]$Representation <- "Authorship"
  all_decompositions[all_decompositions$commit == 100,]$Representation <- "Files"
  all_decompositions[all_decompositions$access == 0 &
                       all_decompositions$write == 0 & 
                       all_decompositions$read == 0 & 
                       all_decompositions$sequence == 0 & 
                       (all_decompositions$commit != 100 & all_decompositions$authors != 100),]$Representation <- "Development history"
  all_decompositions[(all_decompositions$access > 0 | 
                        all_decompositions$write > 0 | 
                        all_decompositions$read > 0 | 
                        all_decompositions$sequence > 0) & 
                       (all_decompositions$commit > 0 | all_decompositions$authors > 0),]$Representation <- "Development history and sequences"
  return(all_decompositions)
}

initialize_tsr_data <- function(tsr_result_location) {
  tsr_data <- read.csv(tsr_result_location, colClasses=c("factor", "numeric", "factor", "numeric","numeric","numeric","numeric","numeric","numeric","character"))
  tsr_data$n_clusters <- factor(tsr_data$n_clusters, levels=c("3", "4", "5", "6", "7", "8", "9", "10"))
  tsr_data$type <- NULL
  
  tsr_data$Representation <- "temp"
  
  tsr_data[tsr_data$commit == 0 & tsr_data$authors == 0,]$Representation <- "Sequences"
  tsr_data[tsr_data$authors == 100,]$Representation <- "Authorship"
  tsr_data[tsr_data$commit == 100,]$Representation <- "Files"
  tsr_data[tsr_data$access == 0 & tsr_data$write == 0 & tsr_data$read == 0 & tsr_data$sequence == 0 & (tsr_data$commit != 100 & tsr_data$authors != 100),]$Representation <- "Development history"
  tsr_data[(tsr_data$access > 0 | tsr_data$write > 0 | tsr_data$read > 0 | tsr_data$sequence > 0) & ( tsr_data$commit > 0 | tsr_data$authors > 0),]$Representation <- "Development history and sequences"
  
  tsr_data$Representation <- factor(tsr_data$Representation, levels=c("Files", "Authorship", "Development history", "Sequences", "Development history and sequences"))
  
  tsr_data$key <- paste(tsr_data$codebase_name, tsr_data$access, tsr_data$write, tsr_data$read, tsr_data$sequence, tsr_data$commit, tsr_data$authors, tsr_data$n_clusters, sep="-")
  return (tsr_data)
}


initialize_data <- function(analyser_result_location, tsr_result_location) {
  all_decompositions <- initialize_analyser_data(analyser_result_location)
  tsr_data <- initialize_tsr_data(tsr_result_location)
  
  all_decompositions$key <- paste(all_decompositions$codebase_name, all_decompositions$access, all_decompositions$write, all_decompositions$read, all_decompositions$sequence, all_decompositions$commit, all_decompositions$authors, all_decompositions$clusters, sep="-")
  all_decompositions <- all_decompositions[all_decompositions$key != "Axon-trader-100-0-0-0-0-0-8",] 
  
  all_data <- inner_join(all_decompositions, tsr_data, by="key") %>%
    dplyr::select(codebase_name.x, pondered_complexity, cohesion, coupling, tsr, access.x, write.x, read.x, sequence.x, commit.x, authors.x, clusters, Representation.x) %>%
    dplyr::mutate(performance = (pondered_complexity + coupling + tsr - cohesion + 1)/4) %>%
    dplyr::rename(codebase_name = codebase_name.x, access = access.x, write = write.x, read = read.x, sequence = sequence.x, commit=commit.x, authors=authors.x, Representation = Representation.x)
  all_data$Representation <- factor(all_data$Representation, levels=c("Files", "Authorship", "Development history", "Sequences", "Development history and sequences"))
  
  return(all_data)
}


# Single cluster comparison  -----------------------------------------------

get_filtered_col <- function(df, metric, clusters, representation) {
  return (df[df$clusters==clusters & df$Representation == representation,]
          %>% dplyr::pull(metric))
}

get_p_value_first_greater <- function(representation1, representation2, metric, clusters, df1, df2) {
  t.test(get_filtered_col(df1, metric, clusters, representation1),
         get_filtered_col(df2, metric, clusters, representation2), "g")$p.value
}
# -------------------------------------------------------------------------

# All clusters comparison  ------------------------------------------------

get_filtered_col_representation <- function(df, metric, representation) {
  return (df[df$Representation == representation,] %>% dplyr::pull(metric))
}

get_p_value_first_greater_all_clusters <- function(representation1, representation2, metric, df1, df2) {
  t.test(get_filtered_col_representation(df1, metric, representation1),
         get_filtered_col_representation(df2, metric, representation2), "g")$p.value
}

# -------------------------------------------------------------------------

best_decompositions_metric_representation <- function(df, metric, representation) {
  if (metric == "cohesion")
    df[df$Representation == representation,] %>% group_by(codebase_name, clusters) %>% slice(which.max(.data[["cohesion"]]))
  else
    df[df$Representation == representation,] %>% group_by(codebase_name, clusters) %>% slice(which.min(.data[[metric]]))
}

best_decompositions_metric <- function(df, metric) {
  representations <- c("Files", "Authorship")
  best <- list(
    Files = best_decompositions_metric_representation(df, metric, "Files"),
    Authorship = best_decompositions_metric_representation(df, metric, "Authorship"),
    DevHistory = best_decompositions_metric_representation(df, metric, "Development history"),
    Sequences = best_decompositions_metric_representation(df, metric, "Sequences"),
    DevHistorySequences = best_decompositions_metric_representation(df, metric, "Development history and sequences")
  )
  return(best)
}

select_best_decompositions <- function(df) {
  best <- list(
    pondered_complexity = best_decompositions_metric(df, "pondered_complexity"),
    cohesion = best_decompositions_metric(df, "cohesion"),
    coupling = best_decompositions_metric(df, "coupling"),
    tsr = best_decompositions_metric(df, "tsr"),
    performance = best_decompositions_metric(df, "performance")
  )
  return (best)
}

bind_large_small_df <- function(df_large, df_small, metric) {
  df_large_metric <- bind_rows(df_large[[metric]])
  df_large_metric$Size <- "Large"
  df_small_metric <- bind_rows(df_small[[metric]])
  df_small_metric$Size <- "Small"
  return(rbind(df_large_metric, df_small_metric))
}

large_vs_small_plot <- function(df_large, df_small, metric, show_legend) {
  if (length(df_large) < 14) {
    large_small <- bind_large_small_df(df_large, df_small, metric)
  } else {
    t1 <- data.frame(df_large)
    t1$Size <- "Large"
    t2 <- data.frame(df_small)
    t2$Size <- "Small"
    large_small <- rbind(t1, t2)
  }
  
  ggplot(large_small, aes_string(x="Representation", y=metric, fill="Size")) +
    geom_boxplot(show.legend=show_legend) +
    scale_y_continuous(limits=c(0,1)) +
    guides(fill=guide_legend(nrow=1)) +
    scale_x_discrete(labels = label_wrap(20))
}

large_vs_small_codebases_p_values <- function(df_large, df_small) {
  metrics <- c("pondered_complexity", "cohesion", "coupling", "tsr", "performance")
  representations <- c("Files", "Authorship", "Development history", "Development history and sequences", "Sequences")
  sizes <- c("Large", "Small")
  
  comparison_pvalues_df <- expand.grid(list(representation_greater=representations, representation_smaller=representations, test_is_greater=sizes, smaller=sizes, metric=metrics))
  comparison_pvalues_df <- comparison_pvalues_df[!(comparison_pvalues_df$test_is_greater == comparison_pvalues_df$smaller &
                                                 comparison_pvalues_df$representation_greater == comparison_pvalues_df$representation_smaller),]
  
  comparison_pvalues <- lapply(1:nrow(comparison_pvalues_df), function(x) {
    representation_greater <- comparison_pvalues_df[x, c("representation_greater")]
    representation_smaller <- comparison_pvalues_df[x, c("representation_smaller")]
    test_greater <- comparison_pvalues_df[x, c("test_is_greater")]
    smaller <- comparison_pvalues_df[x, c("smaller")]
    metric <- comparison_pvalues_df[x, c("metric")]
    
    large_group_greater_median <- round(median(get_filtered_col_representation(bind_rows(df_large[[metric]]),metric,as.character(representation_greater))), 2)
    large_group_smaller_median <- round(median(get_filtered_col_representation(bind_rows(df_large[[metric]]),metric,as.character(representation_smaller))), 2)
    small_group_smaller_median <- round(median(get_filtered_col_representation(bind_rows(df_small[[metric]]),metric,as.character(representation_smaller))), 2)
    small_group_greater_median <- round(median(get_filtered_col_representation(bind_rows(df_small[[metric]]),metric,as.character(representation_greater))), 2)
    

    if (test_greater == "Large" & smaller == "Small") {
      pvalue <- round(get_p_value_first_greater_all_clusters(as.character(representation_greater), as.character(representation_smaller), as.character(metric), bind_rows(df_large[[metric]]), bind_rows(df_small[[metric]])), 3)
      if (pvalue <= 0.05) {
        return (paste("Large group (", large_group_greater_median, ") > small group (", small_group_smaller_median,");p-value:", pvalue));
      } else {
        return (paste("Can't say large group (", large_group_greater_median, ") > other (", small_group_smaller_median,");p-value:", pvalue));
      }
    } else if (test_greater == "Small" & smaller == "Large") {
      pvalue <- round(get_p_value_first_greater_all_clusters(as.character(representation_smaller), as.character(representation_greater), as.character(metric), bind_rows(df_small[[metric]]), bind_rows(df_large[[metric]])), 3)
      if (pvalue <= 0.05) {
        return (paste("Small group (", small_group_greater_median , ") > large group (", large_group_smaller_median ,");p-value:", pvalue));
      } else {
        return (paste("Can't say small group (", small_group_greater_median , ") > large group (", large_group_smaller_median ,");p-value:", pvalue));
      }
    } else if (test_greater == "Large" & smaller == "Large") {
      pvalue <- round(get_p_value_first_greater_all_clusters(as.character(representation_greater), as.character(representation_smaller), as.character(metric), bind_rows(df_large[[metric]]), bind_rows(df_large[[metric]])), 3)
      if (pvalue <= 0.05) {
        return (paste("Large group", representation_greater, "(", large_group_greater_median , ") > large group", representation_smaller,"(", large_group_smaller_median ,");p-value:", pvalue));
      } else {
        return (paste("Can't say large group", representation_greater,  "(", large_group_greater_median , ") > large group (", large_group_smaller_median ,"); p-value:", pvalue));
      }
    } else if (test_greater == "Small" & smaller == "Small") {
      pvalue <- round(get_p_value_first_greater_all_clusters(as.character(representation_greater), as.character(representation_smaller), as.character(metric), bind_rows(df_small[[metric]]), bind_rows(df_small[[metric]])), 3)
      if (pvalue <= 0.05) {
        return (paste("Small group", representation_greater, "(", small_group_greater_median , ") > small group", representation_smaller,"(", small_group_smaller_median ,");p-value:", pvalue));
      } else {
        return (paste("Can't say small group", representation_greater,  "(", small_group_greater_median , ") > small group (", small_group_smaller_median ,");p-value:", pvalue));
      }
    }
  })
  comparison_pvalues_df$pvalues_others_greater <- unlist(comparison_pvalues)
  return (comparison_pvalues_df)
}

representation_vs_representation_p_values <- function(df) {
  metrics <- c("pondered_complexity", "cohesion", "coupling", "tsr", "performance")
  # metrics <- c("pondered_complexity")
  clusters <- c(3,4,5,6,7,8,9,10)
  representations <- c("Files", "Authorship", "Development history", "Development history and sequences", "Sequences")
  
  comparison_pvalues_df <- expand.grid(list(clusters=clusters, test_is_greater=representations, test_is_smaller=representations, metric=metrics))
  comparison_pvalues_df <- comparison_pvalues_df[comparison_pvalues_df$test_is_greater != comparison_pvalues_df$test_is_smaller,]

  comparison_pvalues <- lapply(1:nrow(comparison_pvalues_df), function(x) {
    representation_greater <- comparison_pvalues_df[x, c("test_is_greater")]
    representation_smaller <- comparison_pvalues_df[x, c("test_is_smaller")]
    metric <- comparison_pvalues_df[x, c("metric")]
    clusters <- comparison_pvalues_df[x, c("clusters")]
    
    greater_test_median <- round(median(get_filtered_col_representation(df, metric, representation_greater)), 3)
    smaller_test_median <- round(median(get_filtered_col_representation(df, metric, representation_smaller)), 3)
    
    round(get_p_value_first_greater(representation_greater, representation_smaller, metric, clusters, df, df), 3)
  })
  comparison_pvalues_df$pvalues <- unlist(comparison_pvalues)
  return (comparison_pvalues_df)
}

best_representation_metric_pvalues <- function(df, metric) {
  representations <- c("Files", "Authorship", "Development history", "Development history and sequences", "Sequences")
  
  comparison_pvalues_df <- expand.grid(list(test_is_greater=representations, test_is_smaller=representations))
  comparison_pvalues_df <- comparison_pvalues_df[comparison_pvalues_df$test_is_greater != comparison_pvalues_df$test_is_smaller,]
  
  comparison_pvalues <- lapply(1:nrow(comparison_pvalues_df), function(x) {
    representation_greater <- comparison_pvalues_df[x, c("test_is_greater")]
    representation_smaller <- comparison_pvalues_df[x, c("test_is_smaller")]
    
    greater_test_median <- round(median(get_filtered_col_representation(df, metric, representation_greater)), 3)
    smaller_test_median <- round(median(get_filtered_col_representation(df, metric, representation_smaller)), 3)
    
    round(get_p_value_first_greater_all_clusters(representation_greater, representation_smaller, metric, df, df), 3)
  })
  comparison_pvalues_df$pvalues <- unlist(comparison_pvalues)
  return (comparison_pvalues_df)
}

