// Simple Hardhat deployment script for MiVoto contracts.
// Ejecutar con: npx hardhat run --network <network> contracts/deploy.sample.js
// Requiere configurar network y cuenta en hardhat.config.js

const { ethers } = require('hardhat')

async function main() {
  const [deployer] = await ethers.getSigners()
  console.log('Deploying with account:', deployer.address)

  const SBT = await ethers.getContractFactory('MiVotoSoulboundToken')
  const sbt = await SBT.deploy(deployer.address)
  await sbt.waitForDeployment()
  console.log('MiVotoSoulboundToken:', sbt.target)

  const Election = await ethers.getContractFactory('MiVotoElection')
  const election = await Election.deploy(sbt.target)
  await election.waitForDeployment()
  console.log('MiVotoElection:', election.target)

  const tx = await sbt.setMinter(election.target)
  await tx.wait()
  console.log('SBT minter configurado')
}

main().catch((error) => {
  console.error(error)
  process.exitCode = 1
})
