<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
<title>Traducteur de sous-titres</title>
</head>
<body>
	<form method="post" action="translator" enctype="multipart/form-data">
		<fieldset>
			<legend>Gestion des fichiers : </legend>

			<!--     	
        <p>
            <label for="description">Description du fichier : </label>
            <input type="text" name="description" id="description" />
        </p>
    	
    	-->
			<fieldset>

				<p>
					<label for="fichier">Sélection du fichier à traduire : </label> <input
						type="file" name="fichier" VALUE=" Charger " id="fichier" />
				</p>
				<p>
					<input type="submit" />
				</p>
			</fieldset>
			<fieldset>
				<c:if test="${ !empty error }">
					<p style="color: red;">
						<c:out value="${ error }" />
					</p>
				</c:if>

				<table>
					<tr>
						<p>
							<c:if test="${ !empty dumpedFileName }">
								<p>
									Fichier traduit : <font color="green"> <c:out
											value="${ dumpedFileName }" />
									</font> <font color="green"> </font>
								</p>
							</c:if>
						</p>
					</tr>
					<c:if test="${ !empty subtitleFileName }">
						<tr>
							Traduction du fichier :
							<font color="blue"> <c:out value="${ subtitleFileName }" />
							</font>
						</tr>
						<tr>
							<c:if test="${ !empty subtitleFileName }">
				        	Lignes traduites : <font color="blue"> <c:out
										value="${ oSubtitleStats.getTranslatedLines() }" />
								</font> / <font color="blue"> <c:out
										value="${ oSubtitleStats.getOriginalLines() }" />
								</font>
							</c:if>
						</tr>

							<p>
								Ecrire le fichier de traduction : <input type="submit"
									VALUE=" Ecrire " name="DUMP" />
							</p>

							<p>
								Réinitialiser la traduction : <input type="submit"
									VALUE=" Reset " name="RESET" />
							</p>
							<p>
								Moteur de la base de donées : <font color="blue"> <c:out
										value="${ oSubtitleStats.getJdbcEngine() }" />
								</font>
							</p>
					</c:if>

				</table>
			</fieldset>
		</fieldset>
		
		<table>
			<c:forEach items="${ oSubtitleSequenceArrayList }" var="subtitleSequence"
				varStatus="subtitleSequenceStatus">
				<tr>
					<td></td>
					<td></td>
					<td></td>
					<td><c:out
							value="${ subtitleSequence.getSequenceNumber() } : " /> <c:out
							value="[${ subtitleSequence.getStartTime() }" />
						<c:out value=" --> " /> <c:out
							value="${ subtitleSequence.getEndTime() }]" /></td>
				</tr>
				<tr>
					<td style="text-align: right;"><c:out value="${ line }" /></td>
					<c:forEach items="${ subtitleSequence.originalLinesArrayList }"
						var="line" varStatus="lineStatus">
						<tr>
							<td style="text-align: right;"><c:out value="${ line }" />
							</td>
							<td></td>
							<td></td>
							<c:if
								test="${ subtitleSequence.translatedLines.containsKey(lineStatus.index) }"
								var="isTranslated">
								<td><input type="text"
									name="S${ subtitleSequence.sequenceNumber }L${ lineStatus.index }"
									id="line${ lineStatus.index }"
									value="${ subtitleSequence.translatedLines.get(lineStatus.index) }"
									size="35" /></td>
							</c:if>
							<c:if test="${ ! isTranslated }">
								<td><input type="text"
									name="S${ subtitleSequence.sequenceNumber }L${ lineStatus.index }"
									id="line${ lineStatus.index }" size="35" /></td>
							</c:if>
							<td style="text-align: right;"><input type="submit"
								VALUE=" Enregistrer " /></td>
							<c:if test="${ isTranslated }">
								<td style="text-align: right;"><input type="submit"
									VALUE=" Effacer "
									name="C${ subtitleSequence.sequenceNumber }L${ lineStatus.index }" />
								</td>
							</c:if>
						</tr>

					</c:forEach>
				</tr>
				<tr>
					<td></td>
				</tr>
				<tr>
					<td></td>
				</tr>
				<tr>
					<td></td>
				</tr>
				<tr>
					<td></td>
				</tr>
				<tr>
					<td></td>
				</tr>
				<tr>
					<td></td>
				</tr>
				<tr>
					<td></td>
				</tr>
				<tr>
					<td></td>
				</tr>
			</c:forEach>

		</table>
	</form>
</body>
</html>