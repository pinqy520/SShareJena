import os  
from os.path import join, getsize  


def getdirsize(dir):
	size = []
	for root, dirs, files in os.walk(dir):
		size += [getsize(join(root, name)) for name in files]
	return size  


def ana(size_list):
	max_size = max(size_list) + 10
	min_size = min(size_list)
	size_range = max_size - min_size
	page = size_range/10
	distributed = [0,0,0,0,0,0,0,0,0,0]
	for s in size_list:
		pos = int((s - min_size)/page)
		distributed[pos] = distributed[pos] + 1
	return [str(min_size+page*m) + " ~ " + str(min_size+page*(m+1))  for m in range(0,10)],distributed

