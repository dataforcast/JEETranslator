-- phpMyAdmin SQL Dump
-- version 4.5.4.1deb2ubuntu2
-- http://www.phpmyadmin.net
--
-- Client :  localhost
-- Généré le :  Dim 04 Février 2018 à 16:12
-- Version du serveur :  5.7.21-0ubuntu0.16.04.1-log
-- Version de PHP :  7.0.22-0ubuntu0.16.04.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données :  `subtitle_db`
--
CREATE DATABASE IF NOT EXISTS `subtitle_db` DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;
USE `subtitle_db`;

-- --------------------------------------------------------

--
-- Structure de la table `original`
--

DROP TABLE IF EXISTS `original`;
CREATE TABLE `original` (
  `id` int(11) NOT NULL,
  `seqNumber` int(11) NOT NULL,
  `textLine` varchar(80) COLLATE utf8_unicode_ci NOT NULL,
  `file_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `subtitleSequence`
--

DROP TABLE IF EXISTS `subtitleSequence`;
CREATE TABLE `subtitleSequence` (
  `id` int(11) NOT NULL,
  `seqNumber` int(11) NOT NULL,
  `startDate` datetime(3) NOT NULL,
  `endDate` datetime(3) NOT NULL,
  `file_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `subtitle_file`
--

DROP TABLE IF EXISTS `subtitle_file`;
CREATE TABLE `subtitle_file` (
  `id` int(11) NOT NULL,
  `name` varchar(80) COLLATE utf8_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `translation`
--

DROP TABLE IF EXISTS `translation`;
CREATE TABLE `translation` (
  `id` int(11) NOT NULL,
  `seqNumber` int(11) NOT NULL,
  `lineNumber` int(11) NOT NULL,
  `textLine` varchar(80) COLLATE utf8_unicode_ci NOT NULL,
  `file_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Index pour les tables exportées
--

--
-- Index pour la table `original`
--
ALTER TABLE `original`
  ADD PRIMARY KEY (`id`),
  ADD KEY `file_id` (`file_id`),
  ADD KEY `seqNumber` (`seqNumber`);

--
-- Index pour la table `subtitleSequence`
--
ALTER TABLE `subtitleSequence`
  ADD PRIMARY KEY (`id`),
  ADD KEY `seqNumber` (`seqNumber`),
  ADD KEY `file_id` (`file_id`);

--
-- Index pour la table `subtitle_file`
--
ALTER TABLE `subtitle_file`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Index pour la table `translation`
--
ALTER TABLE `translation`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `triple_id` (`seqNumber`,`lineNumber`,`file_id`),
  ADD KEY `seqNumber` (`seqNumber`),
  ADD KEY `lineNumber` (`lineNumber`),
  ADD KEY `file_id` (`file_id`);

--
-- AUTO_INCREMENT pour les tables exportées
--

--
-- AUTO_INCREMENT pour la table `original`
--
ALTER TABLE `original`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT pour la table `subtitleSequence`
--
ALTER TABLE `subtitleSequence`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT pour la table `subtitle_file`
--
ALTER TABLE `subtitle_file`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT pour la table `translation`
--
ALTER TABLE `translation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
