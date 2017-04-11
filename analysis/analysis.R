#!/usr/local/bin/rscript

require(ggplot2)
require(reshape2)

experiments = c('current', 'vip', 'vipbonus')

colSd <- function (x, na.rm = F) apply(X = x, MARGIN = 2, FUN = sd, na.rm = na.rm)

# http://stackoverflow.com/a/24241954/1185
fancy_scientific <- function(value) {
     value <- format(value, scientific = TRUE)
     value <- gsub("^(.*)e", "'\\1'e", value)
     value <- gsub("e", "%*%10^", value)
     parse(text = value)
}

analysis <- function (plot, title, ylabel, fancy) {
	
	# Read the relevent data from disk
	data <- list()
	for (experiment in experiments) {
		path = paste('../out/', experiment, '/', plot, '.csv', sep="")
		working <- read.csv(path, header=F)

		# Both of these are kind of bad form
		# Convert acres to hectares as needed
		if (plot == "recreation") {
			working = working / 247.11
		}
		# We assume that the number of rows stays the same
		rows = nrow(working)		
	
		data[[experiment]] <- working[, 1:40]
	}
		
	# Prepare the data farme wit the mean of the data
	df <- data.frame(Year=1:40,
					 'current'=colMeans(data[['current']]),
					 'vip'=colMeans(data[['vip']]),
					 'vipbonus'=colMeans(data[['vipbonus']]))
	
	df <- melt(df, id.vars = 'Year', variable.name = 'Series')
	df$Series <- as.character(df$Series)
	df$Series[df$Series == "current"] <- "Current"
	df$Series[df$Series == "vip"] <- "VIP, no bonus"
	df$Series[df$Series == "vipbonus"] <- "VIP, with bonus"
	
	title = sprintf("%s (mean of %i runs)", title, rows)
	
	plotted <- ggplot(df, aes(Year, value)) +
				geom_line(aes(colour = Series)) +
				labs(y = ylabel, title = title)
				
	if (fancy) {
		plotted <- plotted + scale_y_continuous(labels = fancy_scientific)
	}

	file = paste(plot, '.png', sep="")
	ggsave(filename = file, plot = plotted)
}

analysis('biomass', 'Harvested Biomass', 'Green Tons (GT)', T)
analysis('carbon', 'Carbon Sequestration', expression('Gigatons (GtCO'[2]*')'), F)
analysis('recreation', 'Open Access Forest', 'Forest Area (sq. km.)', T)
analysis('vip', 'VIP Enrollment', 'Owners', F)
