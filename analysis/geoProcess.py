#!/usr/bin/python

"""geoProcess.py: A geoprocessing script for a ForestSim experiment using ArcPy"""

import arcpy
import datetime
import os
import re

ROADS_BUFFER = r"E:\LUP\data\wup_roads_buffer.shp"
SCRATCH_FILE = r"E:\LUP\scratch\out.shp"

PATH = r"E:\LUP\data\out"
PROJECT = r"E:\LUP\LUP.mxd"

EXPERIMENTS = ["none", "discount", "agglomeration"]

# https://stackoverflow.com/a/4836734/1185
def natural_sort(l): 
    convert = lambda text: int(text) if text.isdigit() else text.lower() 
    alphanum_key = lambda key: [ convert(c) for c in re.split('([0-9]+)', key) ] 
    return sorted(l, key = alphanum_key)


def analysis(infc, experiment):
	print(infc)
	prepareShapefile(infc)
	area = aesthetics(infc)
	moransI = habitatConnectivity(infc)
	print(area, moransI)
	writeOutput(area, moransI, experiment)
	clearLayers()


def clearLayers():
	mxd = arcpy.mapping.MapDocument(PROJECT)
	for df in arcpy.mapping.ListDataFrames(mxd):
		for lyr in arcpy.mapping.ListLayers(mxd, "", df):
			arcpy.mapping.RemoveLayer(df, lyr)
	mxd.save()
	del mxd
	arcpy.RefreshTOC()
	
	
# Update the shapefile to run correctly
def prepareShapefile(infc):
    # Set the projection
    sr = arcpy.SpatialReference("NAD 1983 UTM Zone 16N")
    arcpy.DefineProjection_management(infc, sr)
    
    # Repair the geometry
    arcpy.RepairGeometry_management(infc)
    
	
def aesthetics(infc):	
	# Clip the input to get the roads buffer
	arcpy.Clip_analysis(infc, ROADS_BUFFER, SCRATCH_FILE)
	
	# Calculate the area
	arcpy.AddField_management(SCRATCH_FILE, "AREA", "double")
	expression = "!SHAPE.AREA@SQUAREKILOMETERS!"
	arcpy.CalculateField_management(SCRATCH_FILE, "AREA", expression, "PYTHON")

	# Sum the area that is less than 12.5 cm
	arcpy.MakeFeatureLayer_management (SCRATCH_FILE, "out")
	arcpy.SelectLayerByAttribute_management("out", "NEW_SELECTION", ' "DBH" < 12.5 ')
	area = sum([r[0] for r in arcpy.da.SearchCursor("out", ["AREA"])])  
	
	# Clean-up and return
	arcpy.Delete_management(SCRATCH_FILE)
	return area
	
	
def habitatConnectivity(infc):	
	# Note the small growth forest ( < 12.5 cm)
	arcpy.AddField_management(infc, "CUT", "short")
	expression = "!DBH! < 12.5"
	arcpy.CalculateField_management(infc, "CUT", expression, "PYTHON")
	
	# Calculate the spatial autocorrelation
	result = arcpy.SpatialAutocorrelation_stats(infc, "CUT", "NO_REPORT", "INVERSE_DISTANCE", "EUCLIDEAN_DISTANCE", "NONE")
	return result

	
# Loop over all of the experiments and the data collected
def process(): 
	print("Starting Geoprocessing...")
	for experiment in EXPERIMENTS:
		for dirName, subdirList, fileList in os.walk(PATH + "\\" + experiment):
			for fileName in natural_sort(fileList):
				if (fileName.endswith(".shp")):
					file = dirName + "\\" + fileName
					analysis(file, experiment)
			writeNewPass(experiment)


def writeNewPass(experiment):
	print('Timestamp: {:%Y-%m-%d %H:%M:%S}'.format(datetime.datetime.now()))
	with open(PATH + "\\" + experiment + r"\aesthetics.csv", "a") as file:
		file.write("\n")
	with open(PATH + "\\" + experiment + r"\habitatConnectivity.csv", "a") as file:
		file.write("\n")

					
def writeOutput(area, moransI, experiment):
	with open(PATH + "\\" + experiment + r"\aesthetics.csv", "a") as file:
		file.write(str(area) + ",")		
	with open(PATH + "\\" + experiment + r"\habitatConnectivity.csv", "a") as file:
		file.write(str(moransI) + ",")


if __name__ == '__main__':
	# Setup the enviroment
	if not os.path.exists(r"E:\LUP\scratch"):
		os.mkdir(r"E:\LUP\scratch")
	if os.path.exists(SCRATCH_FILE):
		arcpy.Delete_management(SCRATCH_FILE)
	arcpy.env.overwriteOutput = True

	# Hand things off
	process()
    