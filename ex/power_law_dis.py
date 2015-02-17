import random, math, os

class PowerLawDis(object):
	"""docstring for PowerLawDis"""
	def __init__(self):
		super(PowerLawDis, self).__init__()
		self.squre_pi = math.pi*math.pi
		self.power_account = 6.0/self.squre_pi
		self.param_c = 0
				
	def get_triple_list(self, rdf_file):
		prefix_list = []
		triple_list = []
		with open(rdf_file) as r_file:
			file_lines = r_file.readlines()
			for line in file_lines:
				line = line.strip()
				if line != '':
					if line.startswith('@prefix'):
						prefix_list.append(line)
					else:
						triple_list.append(line)
		return prefix_list, triple_list


	def power_law_function(self, x):
		#print self.param_c
		return self.param_c*(x**(-2))

	def to_random_file(self, prefix, triple, number):
		randon_int_num = random.randint(0, number)
		file_name = '0/%d.n3' % randon_int_num
		if not os.path.exists(file_name):
			with open(file_name, 'a+') as rf:
				for ep in prefix:
					rf.write(ep)
					rf.write('\n')
		with open(file_name, 'a+') as rf:
			rf.write(triple)
			rf.write('\n')

	def to_file(self, x, triple_list, sum_num):
		for t in triple_list:
			num_list = random.sample(range(0, sum_num), x)
			for n in num_list:
				file_name = '0/%d.n3' % n
				with open(file_name, 'a+') as rf:
					rf.write(t)
					rf.write('\n')

	def test_func(self):
		prefixes, triples = self.get_triple_list('sp2b.n3')
		#print self.power_account
		self.param_c = self.power_account*len(triples)
		#print self.param_c
		for x in range(1, 101):
			y = self.power_law_function(x)
			print x
			print y

	def rec(self, rec_file, content):
		with open(rec_file, 'a+') as rf:
			rf.write(str(content))
			rf.write('\n')

	def distribute(self, src_file):
		file_num = 100
		prefixes, triples = self.get_triple_list(src_file)
		count = 0
		files = []
		self.param_c = self.power_account*len(triples)
		#print self.param_c
		random.shuffle(triples)
		for i in range(0, 100):
			file_name = '0/%d.n3' % i
			with open(file_name, 'a+') as rf:
				for ep in prefixes:
					rf.write(ep)
					rf.write('\n')
		for x in range(1, 101):
			self.rec('0/rec.txt', x)
			print x
			y = self.power_law_function(x)
			#print y
			y = int(y)
			sub_list = []
			if y+count > len(triples):
				sub_list = triples[count : len(triples)]
				self.to_file(x, sub_list, file_num)
				return
			else:
				sub_list = triples[count : y+count]
				self.to_file(x, sub_list, file_num)
			count = count + y
			self.rec('0/rec.txt', ('%d, %d') % (count, len(triples)))
			print ('%d, %d') % (count, len(triples))
			self.rec('0/rec.txt', '------------------------------')
			print '------------------------------'
		self.to_file(1, triples[count : len(triples)], file_num)

pl = PowerLawDis()
pl.distribute('sp2b.n3')