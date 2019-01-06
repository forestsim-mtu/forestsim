#!/usr/local/bin/rscript

require(ggplot2)
require(reshape2)

experiments = c('none', 'discount', 'agglomeration')
policyIntroduction = 49 
timeSteps = 199

colSd <- function (x, na.rm = F) apply(X = x, MARGIN = 2, FUN = sd, na.rm = na.rm)

# http://stackoverflow.com/a/24241954/1185
fancy_scientific <- function(value) {
     value <- format(value, scientific = TRUE)
     value <- gsub("^(.*)e", "'\\1'e", value)
     value <- gsub("e", "%*%10^", value)
     parse(text = value)
}

analysis <- function (file, title, ylabel, fancy) {
	
	# Read the relevent data from disk
	data <- loadData(file)
	rows = nrow(data[['none']])
	
	# Prepare the data farme with the mean of the data
	df <- data.frame(Year = 1:timeSteps,
					 'none' = colMeans(data[['none']]),
					 'discount' = colMeans(data[['discount']]),
					 'agglomeration' = colMeans(data[['agglomeration']]))
		
	df <- melt(df, id.vars = 'Year', variable.name = 'Series')
	df$Series <- as.character(df$Series)
	df$Series[df$Series == "none"] <- "No VIP"
	df$Series[df$Series == "discount"] <- "VIP, millage bonus"
	df$Series[df$Series == "agglomeration"] <- "VIP, agglomeration bonus"
	
	# Plot and save
	plot(df, rows, title, ylabel, fancy, file)
}

harvestAnalysis <- function(title, ylabel, fancy) {
	# Read the relevent data from disk
	stems <- loadData('harvestedStems')
	biomass <- loadData('harvestedBiomass')
	rows = nrow(stems[['none']])
		
	# Prepare the data frame wtih the mean of the data
	df <- data.frame(Year = 1:timeSteps,
			'none.biomass' = colMeans(biomass[['none']]),
			'none.stems' = colMeans(stems[['none']]),
			'discount.biomass' = colMeans(biomass[['discount']]),
			'discount.stems' = colMeans(stems[['discount']]),
			'agglomeration.biomass' = colMeans(biomass[['agglomeration']]),
			'agglomeration.stems' = colMeans(stems[['agglomeration']]))
	
	# Melt and set the labels
	df <- melt(df, id.vars = 'Year', variable.name = 'Series')
	df$Series <- as.character(df$Series)
	df$Series[df$Series == "none.biomass"] <- "Biomass - No VIP"
	df$Series[df$Series == "none.stems"] <- "Stems - No VIP"
	df$Series[df$Series == "discount.biomass"] <- "Biomass - VIP, millage bonus"
	df$Series[df$Series == "discount.stems"] <- "Stems - VIP, millage bonus"
	df$Series[df$Series == "agglomeration.biomass"] <- "Biomass - VIP, agglomeration bonus"
	df$Series[df$Series == "agglomeration.stems"] <- "Stems - VIP, agglomeration bonus"

	# Plot and save
	plot(df, nrow(stems), title, ylabel, fancy, 'harvestedBiomass')
}

loadData <- function(file) {
	data <- list()
	for (experiment in experiments) {
		path = paste('../out/', experiment, '/', file, '.csv', sep="")
		working <- read.csv(path, header=F)
		data[[experiment]] <- working[, 0:timeSteps]
	}
	return(data)
}

plot <- function(df, rows, title, ylabel, fancy, file) {
	title = sprintf("%s (mean of %i runs)", title, rows)
	plotted <- ggplot(df, aes(Year, value)) +
			geom_vline(xintercept = policyIntroduction) +
			geom_line(aes(colour = Series)) +
			labs(y = ylabel, title = title) +
			theme(legend.position = "bottom", legend.title = element_blank())
	
	if (fancy) {
		plotted <- plotted + scale_y_continuous(labels = fancy_scientific)
	}
	
	file = paste(file, '.png', sep="")
	ggsave(filename = file, plot = plotted)	
}

analysis('carbonGlobal', 'Global Carbon Sequestration', expression('Metric Tons (MTCO'[2]*')'), F)
analysis('recreationAccess', 'Open Access Forest', 'Forest Area (sq. km.)', F)
analysis('harvestedBiomass', 'Harvested Biomass', 'Metric Tons (MT) Dry Weight', T)

