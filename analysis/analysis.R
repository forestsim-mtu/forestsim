#!/usr/local/bin/rscript

require(ggplot2)
require(reshape2)

experiments = c('none', 'discount', 'agglomeration')
timeSteps = 105

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

		# This is all kind of bad form
		# Convert acres to hectares as needed
		if (plot == "recreation") {
			working = working / 247.11
		}
		
		# We assume that the number of rows stays the same
		rows = nrow(working)		
	
		data[[experiment]] <- working[, 1:timeSteps]
	}
		
	# Prepare the data farme wit the mean of the data
	df <- data.frame(Year=1:timeSteps,
					 'none'=colMeans(data[['none']]),
					 'discount'=colMeans(data[['discount']]),
					 'agglomeration'=colMeans(data[['agglomeration']]))
	
	df <- melt(df, id.vars = 'Year', variable.name = 'Series')
	df$Series <- as.character(df$Series)
	df$Series[df$Series == "none"] <- "No VIP"
	df$Series[df$Series == "discount"] <- "VIP, millage bonus"
	df$Series[df$Series == "agglomeration"] <- "VIP, agglomeration bonus"
	
	title = sprintf("%s (mean of %i runs)", title, rows)
	
	plotted <- ggplot(df, aes(Year, value)) +
				geom_line(aes(colour = Series)) +
				labs(y = ylabel, title = title) +
				theme(legend.position = "bottom", legend.title = element_blank())
				
	if (fancy) {
		plotted <- plotted + scale_y_continuous(labels = fancy_scientific)
	}
	
	# if (plot == "biomass") {
		# plotted <- plotted + geom_hline(yintercept = 100000000);
	# }

	file = paste(plot, '.png', sep="")
	ggsave(filename = file, plot = plotted)
}

analysis('biomass', 'Harvested Biomass', 'Kilograms Dry Weight', T)
analysis('carbonAgents', 'Carbon Sequestration by NIPFOs', expression('Gigatons (GtCO'[2]*')'), F)
analysis('carbonGlobal', 'Global Carbon Sequestration', expression('Gigatons (GtCO'[2]*')'), F)
analysis('demand', 'Harvest Demand', 'Owners', F)
analysis('harvested', 'Harvested Parcels', 'Owners', F)
analysis('recreation', 'Open Access Forest', 'Forest Area (sq. km.)', T)
analysis('vip', 'VIP Enrollment', 'Owners', F)
