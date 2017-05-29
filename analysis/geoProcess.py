#!/usr/bin/python

"""geoProcess.py: A geoprocessing script for a ForestSim experiment using ArcPy"""

#import arcypy
import os

rootdir = "../out"
experiments = ["none", "discount", "agglomeration"]

# Update the shapefile to run correctly
def prepareShapefile(infc):
    print(infc)
    
    # Set the projection
    sr = arcpy.SpatialReference("NAD 1983 UTM Zone 16N")
    arcpy.DefineProjection_management(infc, sr)
    
    # Repair the geometry
    acrpy.RepairGeometry_management(infc)
    
    # Calculate the area
    acrpy.AddField_management(infc, "AREA", "double")
    expression = "{0}".format("!SHAPE.area@SQUAREKILOMETERS")
    acrpy.CalculateField_management(infc, "AREA", expression, "PYTHON")
       

# Loop over all of the experiments and the data collected 
for experiment in experiments:
    for dirName, subdirList, fileList in os.walk(rootdir + "/" + experiment):
        for fileName in fileList:
            if (fileName.endswith(".shp")):
                prepareShapefile(dirName + "/" + fileName)



    