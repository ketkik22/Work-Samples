import igraph
import operator

graph = igraph.Graph.Read_Edgelist('0.edges', directed=False)
print "No of nodes = {}".format(graph.vcount())
print "No of edges = {}".format(graph.ecount())
print "Diameter = {}".format(graph.diameter(directed=False))

clusteringCoefficientsList = graph.transitivity_local_undirected(vertices=None)
#print clusteringCoefficientsList  
list1 = sorted(range(len(clusteringCoefficientsList)), key=lambda i: clusteringCoefficientsList[i])[-5:]
print "Five nodes with the highest clustering coefficients are {}, {}, {}, {}, {}".format(list1[0], list1[1], list1[2], list1[3], list1[4])

vertexBetweennessList = graph.betweenness(vertices=None, directed=False)
#print vertexBetweennessList
list2 = sorted(range(len(vertexBetweennessList)), key=lambda i: vertexBetweennessList[i])[-5:]   
print "Five nodes with the highest vertex betweenness are {}, {}, {}, {}, {}".format(list2[0], list2[1], list2[2], list2[3], list2[4])

visual_style = {}
visual_style["edge_curved"] = False
visual_style["vertex_color"] = "#95D2CB"
visual_style["vertex_size"] = 11
visual_style["vertex_label_size"] = 12

igraph.plot(graph, "facebookNetwork.png", **visual_style)

visual_style1 = {}
visual_style1["edge_curved"] = False
visual_style1["vertex_size"] = 11
visual_style1["vertex_label_size"] = 12

louvainCommunity = graph.community_multilevel()
print "No of community = {}".format(louvainCommunity.cluster_graph().vcount())
print "Modularity of communities detected by Louvain Algorithm on the Facebook dataset =  {}".format(louvainCommunity.modularity)
igraph.plot(louvainCommunity, "LouvainOnFacebook.png", **visual_style1)

girvanNewmanCommunity = graph.community_edge_betweenness().as_clustering()
print "No of community = {}".format(girvanNewmanCommunity.cluster_graph().vcount())
print "Modularity of communities detected by Girvan-Newman Algorithm on the Facebook dataset =  {}".format(girvanNewmanCommunity.modularity)
igraph.plot(girvanNewmanCommunity, "GirvanNewmanOnFacebook.png", **visual_style1)

labelPropagationCommunity = graph.community_label_propagation()
#print "No of community = {}".format(labelPropagationCommunity.cluster_graph().vcount())
print "Modularity of communities detected by Label Propagation Algorithm on the Facebook dataset =  {}".format(labelPropagationCommunity.modularity)
igraph.plot(labelPropagationCommunity, "LabelPropagationOnFacebook.png", **visual_style)

#fastGreedyCommunity = graph.community_fastgreedy().as_clustering()
#print "Modularity of communities detected by Fast-greedy Algorithm on the Facebook dataset =  {}".format(fastGreedyCommunity.modularity)
#igraph.plot(fastGreedyCommunity, "FastGreedyOnFacebook.png", **visual_style1)

randomWalkCommunity = graph.community_walktrap().as_clustering()
print "No of community = {}".format(randomWalkCommunity.cluster_graph().vcount())
print "Modularity of communities detected by Random walk Algorithm on the Facebook dataset =  {}".format(randomWalkCommunity.modularity)
igraph.plot(randomWalkCommunity, "RandomWalkOnFacebook.png", **visual_style1)
